package org.apache.lucene.search.positions;

import java.io.IOException;

import org.apache.lucene.search.Scorer;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class BrouwerianIntervalIterator extends PositionIntervalIterator {
  
  private final PositionIntervalIterator minuted;
  private final PositionIntervalIterator subtracted;
  private PositionInterval subtractedInterval = new PositionInterval();
  private PositionInterval currentInterval = new PositionInterval();
  private int secondDoc = -1;

  public BrouwerianIntervalIterator(Scorer scorer, boolean collectPositions, PositionIntervalIterator minuted, PositionIntervalIterator subtracted) {
    super(scorer, collectPositions);
    this.minuted = minuted;
    this.subtracted = subtracted;
  }
  

  @Override
  public int advanceTo(int docId) throws IOException {
    currentDoc = minuted.advanceTo(docId);
    secondDoc  = subtracted.advanceTo(docId);
    subtractedInterval.reset();
    return currentDoc;
  }
  
  @Override
  public PositionInterval next() throws IOException {
    if (secondDoc != currentDoc) {
      return currentInterval = minuted.next();
    }
    while ((currentInterval = minuted.next()) != null) {
      while(subtractedInterval.lessThan(currentInterval) && (subtractedInterval = subtracted.next()) != null) {
      }
      if (subtractedInterval == null || subtractedInterval.greaterThan(currentInterval)) {
        return currentInterval;
      }
    }
    return currentInterval;
  }
  
  @Override
  public void collect(PositionCollector collector) {
    assert collectPositions;
    collector.collectComposite(scorer, currentInterval, currentDoc);
    minuted.collect(collector);
    
  }
  
  @Override
  public PositionIntervalIterator[] subs(boolean inOrder) {
    return new PositionIntervalIterator[] {minuted, subtracted};
  }


  @Override
  public int matchDistance() {
    return minuted.matchDistance();
  }
  
}
