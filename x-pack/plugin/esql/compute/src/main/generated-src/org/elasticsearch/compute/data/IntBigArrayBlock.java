/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */

package org.elasticsearch.compute.data;

import org.apache.lucene.util.RamUsageEstimator;
import org.elasticsearch.common.util.IntArray;
import org.elasticsearch.core.Releasables;

import java.util.BitSet;

/**
 * Block implementation that stores values in a IntArray.
 * This class is generated. Do not edit it.
 */
public final class IntBigArrayBlock extends AbstractArrayBlock implements IntBlock {

    private static final long BASE_RAM_BYTES_USED = 0; // TODO: fix this
    private final IntArray values;

    public IntBigArrayBlock(
        IntArray values,
        int positionCount,
        int[] firstValueIndexes,
        BitSet nulls,
        MvOrdering mvOrdering,
        BlockFactory blockFactory
    ) {
        super(positionCount, firstValueIndexes, nulls, mvOrdering, blockFactory);
        this.values = values;
    }

    @Override
    public IntVector asVector() {
        return null;
    }

    @Override
    public int getInt(int valueIndex) {
        return values.get(valueIndex);
    }

    @Override
    public IntBlock filter(int... positions) {
        try (var builder = blockFactory().newIntBlockBuilder(positions.length)) {
            for (int pos : positions) {
                if (isNull(pos)) {
                    builder.appendNull();
                    continue;
                }
                int valueCount = getValueCount(pos);
                int first = getFirstValueIndex(pos);
                if (valueCount == 1) {
                    builder.appendInt(getInt(getFirstValueIndex(pos)));
                } else {
                    builder.beginPositionEntry();
                    for (int c = 0; c < valueCount; c++) {
                        builder.appendInt(getInt(first + c));
                    }
                    builder.endPositionEntry();
                }
            }
            return builder.mvOrdering(mvOrdering()).build();
        }
    }

    @Override
    public ElementType elementType() {
        return ElementType.INT;
    }

    @Override
    public IntBlock expand() {
        if (firstValueIndexes == null) {
            incRef();
            return this;
        }
        // TODO use reference counting to share the values
        try (var builder = blockFactory().newIntBlockBuilder(firstValueIndexes[getPositionCount()])) {
            for (int pos = 0; pos < getPositionCount(); pos++) {
                if (isNull(pos)) {
                    builder.appendNull();
                    continue;
                }
                int first = getFirstValueIndex(pos);
                int end = first + getValueCount(pos);
                for (int i = first; i < end; i++) {
                    builder.appendInt(getInt(i));
                }
            }
            return builder.mvOrdering(MvOrdering.DEDUPLICATED_AND_SORTED_ASCENDING).build();
        }
    }

    @Override
    public long ramBytesUsed() {
        return BASE_RAM_BYTES_USED + RamUsageEstimator.sizeOf(values) + BlockRamUsageEstimator.sizeOf(firstValueIndexes)
            + BlockRamUsageEstimator.sizeOfBitSet(nullsMask);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IntBlock that) {
            return IntBlock.equals(this, that);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return IntBlock.hash(this);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
            + "[positions="
            + getPositionCount()
            + ", mvOrdering="
            + mvOrdering()
            + ", ramBytesUsed="
            + values.ramBytesUsed()
            + ']';
    }

    @Override
    public void closeInternal() {
        blockFactory().adjustBreaker(-ramBytesUsed() + RamUsageEstimator.sizeOf(values), true);
        Releasables.closeExpectNoException(values);
    }
}
