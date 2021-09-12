import React from 'react';
import { Block } from '../Block';
import { Checkbox } from '../../Checkbox';
import { recurseBlocks } from '../../../../utils/recurseBlocks';
import { toggleBlockProperty } from '../utils/toggleBlockProperty';

export const useChecklist = (blockTree, BlockComponent?) => {
  const [blockState, setBlockState] = React.useState(blockTree);
  const total = Object.keys(blockState.blocks).length;
  const checked = Object.keys(blockState.blocks).filter(uid => blockState.blocks[uid].isChecked).length;

  const blocks = recurseBlocks({
    tree: blockState.tree,
    content: blockState.blocks,
    setBlockState: setBlockState,
    ApplyProps: (block) => ({
      ...block,
      renderedContent: <><Checkbox labelProps={{ style: { marginRight: "0.25em" } }} styleCircle defaultSelected={blockState.blocks[block.uid].isChecked} onChange={() => toggleBlockProperty(block.uid, 'isChecked', setBlockState)} />{blockState.blocks[block.uid].renderedContent}</>,
      handlePressToggle: (uid: UID) => {
        toggleBlockProperty(uid, 'isOpen', setBlockState);
      }
    }),
    blockComponent: BlockComponent ? BlockComponent : <Block />
  });

  return { blocks, checked, total };
};

// import { Block } from '../Block';
// import { Checkbox } from '../../Checkbox';
// import { recurseBlocks } from '../../../../utils/recurseBlocks';
// import { toggleBlockProperty } from '../utils/toggleBlockProperty';

// export const useChecklist = (blocks, setBlocks, BlockComponent?) => {
//   const total = Object.keys(blocks.blocks).length;
//   const checked = Object.keys(blocks.blocks).filter(uid => blocks.blocks[uid].isChecked).length;

//   const returnBlocks = recurseBlocks({
//     tree: blocks.tree,
//     content: blocks.blocks,
//     setBlockState: setBlocks,
//     ApplyProps: (block) => ({
//       ...block,
//       renderedContent: <><Checkbox
//         labelProps={{ style: { marginRight: "0.25em" } }}
//         styleCircle
//         defaultSelected={blocks.blocks[block.uid].isChecked}
//         onChange={() => toggleBlockProperty(block.uid, 'isChecked', setBlocks)}
//       />
//         {blocks.blocks[block.uid].renderedContent}
//       </>,
//       handlePressToggle: (uid: UID) => {
//         toggleBlockProperty(uid, 'isOpen', setBlocks);
//       }
//     }),
//     blockComponent: BlockComponent ? BlockComponent : <Block />
//   });

//   return { blocks, setBlocks, checked, total };
// };
