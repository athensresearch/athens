import React from 'react';
import { BlockProps } from '../components/Block/Block';

interface recurseBlocksProps {
  tree: any[];
  content: any;
  setBlockState?: Function;
  blockProps?: BlockProps;
  blockComponent: any;
  lengthLimit?: number;
  depthLimit?: number;
  incCurrentLength?(): void;
  incCurrentDepth?(): void;
  getCurrentLength?(): number;
  getCurrentDepth?(): number;
}


// makeBlocks
// set currentLenght = 0
// const incCurrentLength = (currentLength) => currentLength++
// 
// recurseBlocks(...stuff, incCurrentLength)

export const recurseBlocks = ({
  tree,
  content,
  setBlockState,
  blockProps,
  blockComponent,
  lengthLimit = Infinity, // FIXME: this doesn't work properly
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
    blockProps,
    blockComponent,
    lengthLimit,
    depthLimit,
    incCurrentLength,
    incCurrentDepth,
    getCurrentLength,
    getCurrentDepth,
  });
}


export const recurseBlocksFn = ({
  tree,
  content,
  setBlockState,
  blockProps,
  blockComponent,
  lengthLimit,
  depthLimit,
  incCurrentLength,
  incCurrentDepth,
  getCurrentLength,
  getCurrentDepth,
}: recurseBlocksProps) => {
  console.log(incCurrentLength);

  return tree.map((block) => {
    incCurrentLength();

    if (lengthLimit >= getCurrentLength()) {
      incCurrentDepth();
      return (
        React.cloneElement(blockComponent,
        {
          key: block.uid,
          uid: block.uid,
          ...content[block.uid],
          ...blockProps,
          children: block.children && (lengthLimit >= getCurrentLength() && depthLimit >= getCurrentDepth()) && recurseBlocksFn({
            tree: block.children,
            content: content,
            setBlockState: setBlockState,
            blockProps: blockProps,
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
      console.log('exiting');
      return
    }
  });
};
