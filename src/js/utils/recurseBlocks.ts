import React from 'react';

interface recurseBlocksProps {
  tree: any[];
  content: any;
  setBlockState?: Function;
  blockProps?: Object;
  blockComponent: any;
}

export const recurseBlocks = ({
  tree,
  content,
  setBlockState,
  blockProps,
  blockComponent
}: recurseBlocksProps) => {
  return tree.map(block => {
    return (
      React.cloneElement(blockComponent,
        {
          ...content[block.uid],
          ...blockProps,
          children: block.children && recurseBlocks({
            tree: block.children,
            content: content,
            setBlockState: setBlockState,
            blockProps: blockProps,
            blockComponent: blockComponent
          }),
        },
      )
    );
  });
};
