import React from 'react';
import { BlockProps } from '../Block';
import { BlockGraph } from '../../../../main';

interface renderBlocksProps {
  blockGraph: BlockGraph;
  setBlockState?: Function;
  ApplyProps?(block: BlockProps): BlockProps;
  blockComponent?: any;
  lengthLimit?: number;
  depthLimit?: number;
  incCurrentLength?(): void;
  incCurrentDepth?(): void;
  getCurrentLength?(): number;
  getCurrentDepth?(): number;
}

export const renderBlocks = ({
  blockGraph,
  setBlockState,
  ApplyProps = () => null,
  blockComponent,
  lengthLimit = Infinity,
  depthLimit = Infinity,
}: renderBlocksProps) => {
  let currentLength = 0;
  let currentDepth = 0;
  const incCurrentLength = () => currentLength++;
  const incCurrentDepth = () => currentDepth++;
  const getCurrentLength = () => currentLength;
  const getCurrentDepth = () => currentDepth;

  return renderBlocksFn({
    blockGraph,
    setBlockState,
    ApplyProps,
    blockComponent,
    lengthLimit,
    depthLimit,
    incCurrentLength,
    incCurrentDepth,
    getCurrentLength,
    getCurrentDepth,
  });
}

const renderBlocksFn = ({
  blockGraph,
  setBlockState,
  ApplyProps,
  blockComponent,
  lengthLimit,
  depthLimit,
  incCurrentLength,
  incCurrentDepth,
  getCurrentLength,
  getCurrentDepth,
}: renderBlocksProps) => {
  return blockGraph.tree.map((block) => {
    incCurrentLength();

    if (lengthLimit >= getCurrentLength()) {
      incCurrentDepth();

      return (
        React.cloneElement(blockComponent, {
          key: block.uid,
          uid: block.uid,
          ...blockGraph.blocks[block.uid],
          ...ApplyProps(block),
          ApplyProps: ApplyProps,
          children: block.children && (lengthLimit >= getCurrentLength() && depthLimit >= getCurrentDepth()) && renderBlocksFn({
            blockGraph: {
              tree: block.children,
              blocks: blockGraph.blocks
            },
            setBlockState: setBlockState,
            ApplyProps: ApplyProps,
            blockComponent: blockComponent,
            lengthLimit: lengthLimit,
            depthLimit: depthLimit,
            incCurrentLength,
            incCurrentDepth,
            getCurrentLength,
            getCurrentDepth,
          }),
        },
        ))
    } else {
      return
    }
  });
};
