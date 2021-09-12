import React from 'react';
import { BlockProps } from '../components/concept/Block/Block';

interface recurseBlocksProps {
  tree: any[];
  content: any;
  setBlockState?: Function;
  ApplyProps?(block: BlockProps): BlockProps; // Any so we can add more props to the block
  blockComponent: any;
  lengthLimit?: number;
  depthLimit?: number;
  incCurrentLength?(): void;
  incCurrentDepth?(): void;
  getCurrentLength?(): number;
  getCurrentDepth?(): number;
}

export const recurseBlocks = ({
  tree,
  content,
  setBlockState,
  ApplyProps = () => null,
  blockComponent,
  lengthLimit = Infinity,
  depthLimit = Infinity,
}: recurseBlocksProps) => {
  let currentLength = 0;
  let currentDepth = 0;
  const incCurrentLength = () => currentLength++;
  const incCurrentDepth = () => currentDepth++;
  const getCurrentLength = () => currentLength;
  const getCurrentDepth = () => currentDepth;

  return recurseBlocksFn({
    tree,
    content,
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

const recurseBlocksFn = ({
  tree,
  content,
  setBlockState,
  ApplyProps,
  blockComponent,
  lengthLimit,
  depthLimit,
  incCurrentLength,
  incCurrentDepth,
  getCurrentLength,
  getCurrentDepth,
}: recurseBlocksProps) => {
  return tree.map((block) => {
    incCurrentLength();

    if (lengthLimit >= getCurrentLength()) {
      incCurrentDepth();

      return (
        React.cloneElement(blockComponent, {
          key: block.uid,
          uid: block.uid,
          ...content[block.uid],
          ...ApplyProps(block),
          ApplyProps: ApplyProps,
          children: block.children && (lengthLimit >= getCurrentLength() && depthLimit >= getCurrentDepth()) && recurseBlocksFn({
            tree: block.children,
            content: content,
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
