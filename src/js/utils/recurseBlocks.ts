import React from 'react';

interface recurseBlocksProps {
  tree: any[];
  content: any;
  setBlockState?: Function;
  blockProps?: Object;
  blockComponent: any;
  currentLength?: number;
  lengthLimit?: number;
  returnLimit?: number;
  depthLimit?: number;
  currentDepth?: number;
}

export const recurseBlocks = ({
  tree,
  content,
  setBlockState,
  blockProps,
  blockComponent,
  lengthLimit = Infinity, // FIXME: this doesn't work properly
  depthLimit = Infinity,
  currentLength = 1,
  currentDepth = 1,
}: recurseBlocksProps) => {
  return tree.map(block => {
    currentLength += 1;

    if (lengthLimit >= currentLength) {
    return (
      React.cloneElement(blockComponent,
        {
          key: block.uid,
          ...content[block.uid],
          ...blockProps,
          children: block.children && (lengthLimit >= currentLength && depthLimit >= currentDepth) && recurseBlocks({
            tree: block.children,
            content: content,
            setBlockState: setBlockState,
            blockProps: blockProps,
            blockComponent: blockComponent,
            lengthLimit: lengthLimit,
            depthLimit: depthLimit,
            currentLength: currentLength++,
            currentDepth: currentDepth++,
          }),
        },
        ))
    } else {
      return
    }
  });
};
