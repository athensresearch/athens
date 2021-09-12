import React from 'react';
import { Block } from '../Block';
import { recurseBlocks } from '../../../../utils/recurseBlocks';
import { toggleBlockProperty } from '../utils/toggleBlockProperty';

export const useSelection = (blockTree, BlockComponent?) => {
  const [blockState, setBlockState] = React.useState(blockTree);

  const blocks = recurseBlocks({
    tree: blockState.tree,
    content: blockState.blocks,
    setBlockState: setBlockState,
    ApplyProps: (block) => ({
      ...block,
      handlePressToggle: () => toggleBlockProperty(block.uid, 'isOpen', setBlockState),
      handlePressContainer: () => toggleBlockProperty(block.uid, 'isSelected', setBlockState),
    }),
    blockComponent: BlockComponent ? BlockComponent : <Block />
  });

  return { blocks };
};
