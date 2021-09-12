import React from 'react';
import { mockPeople } from '../../Avatar/mockData';

import { Block } from './Block';
import { BADGE, Storybook } from '../../../storybook';
import { Meter } from '../Meter';
import { blockTree } from './mockData';
import { recurseBlocks } from '../../../utils/recurseBlocks';
import { usePresence } from './hooks/usePresence';
import { useChecklist } from './hooks/useChecklist';
import { useSelection } from './hooks/useSelection';
import { useBlockState } from './hooks/useBlockState';


export default {
  title: 'Components/Block',
  blockComponent: Block,
  argTypes: {},
  decorators: [(Story) => <Storybook.Wrapper><Story /></Storybook.Wrapper>],
  parameters: {
    badges: [BADGE.DEV]
  }
};

const Template = (args) => <Block {...args} />;

export const Basic = Template.bind({});
Basic.args = {
  ...blockTree.blocks["1"],
};

export const Editing = Template.bind({});
Editing.args = {
  ...blockTree.blocks["1"],
  isEditing: true,
};

export const BlockTree = () => {
  const toggleBlockOpen = (uid, setBlockState) => {
    setBlockState(prevState => {
      return ({
        ...prevState,
        blocks: {
          ...prevState.blocks,
          [uid]: {
            ...prevState.blocks[uid],
            isOpen: !prevState.blocks[uid].isOpen
          }
        }
      })
    })
  }

  const [blockState, setBlockState] = React.useState(blockTree);
  return recurseBlocks({
    tree: blockState.tree,
    content: blockState.blocks,
    setBlockState: setBlockState,
    ApplyProps: (block) => ({
      ...block,
      handlePressToggle: (uid) => {
        toggleBlockOpen(uid, setBlockState)
      }
    }),
    blockComponent: <Block />
  })
}

export const WithPresence = () => {
  const presence = mockPeople.map((p, index) => ({ ...p, uid: index.toString() }));
  const { blocks } = usePresence(blockTree, presence);
  return blocks;
}

// export const WithChecklist = () => {
//   const { blocks, checked, total } = useChecklist(blockTree);
//   return <>
//     <Meter value={checked} maxValue={total} label="Completed" />
//     <br />
//     {blocks}
//   </>;
// }

export const WithChecklist = () => {
  // const { blockState, setBlockState } = useBlockState(blockTree);
  const { blocks, checked, total } = useChecklist(blockTree);
  // const { blocks, setBlocks, checked, total } = useChecklist(blockState, setBlockState);
  return <>
    <Meter value={checked} maxValue={total} label="Completed" />
    <br />
    {blocks}
  </>;
}

export const WithSelection = () => {
  const { blocks } = useSelection(blockTree);
  return <>
    {blocks}
  </>;
}

export const References = Template.bind({});
References.args = {
  ...blockTree.blocks["1"],
  refsCount: 12
};

export const Selected = Template.bind({});
Selected.args = {
  ...blockTree.blocks["1"],
  isSelected: true,
};

export const MultipleSelected = () => {

  const toggleBlockOpen = (uid, setBlockState) => {
    setBlockState(prevState => {
      return ({
        ...prevState,
        blocks: {
          ...prevState.blocks,
          [uid]: {
            ...prevState.blocks[uid],
            isOpen: !prevState.blocks[uid].isOpen
          }
        }
      })
    })
  }

  const [blockState, setBlockState] = React.useState(blockTree);
  return recurseBlocks({
    tree: blockState.tree,
    content: blockState.blocks,
    setBlockState: setBlockState,
    ApplyProps: (block) => ({
      ...block,
      isSelected: true,
      handlePressToggle: () => toggleBlockOpen(block.uid, setBlockState)
    }),
    blockComponent: <Block />
  })
}
