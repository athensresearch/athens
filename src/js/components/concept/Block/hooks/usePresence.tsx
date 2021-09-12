import React from 'react';
import { Block } from '../Block';
import { recurseBlocks } from '../../../../utils/recurseBlocks';
import { toggleBlockProperty } from '../utils/toggleBlockProperty';

export const usePresence = (blockTree, presence, BlockComponent?) => {
  const [blockState, setBlockState] = React.useState(blockTree);
  const [presenceState, setPresenceState] = React.useState(presence);

  const blocks = recurseBlocks({
    tree: blockState.tree,
    content: blockState.blocks,
    setBlockState: setBlockState,
    ApplyProps: (block) => ({
      ...block,
      presentUser: presenceState.find((p: PersonPresence) => p.uid === block.uid),
      handlePressToggle: (uid: UID) => toggleBlockProperty(uid, 'isOpen', setBlockState),
    }),
    blockComponent: BlockComponent ? BlockComponent : <Block />
  });

  return { blocks };
};
