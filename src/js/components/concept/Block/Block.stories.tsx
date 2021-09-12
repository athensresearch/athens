import React from 'react';
import { mockPeople } from '../../Avatar/mockData';

import { Block } from './Block';
import { BADGE, Storybook } from '../../../storybook';
import { Checklist } from '../Checkbox/Checkbox.stories';
import {
  block,
  blockTree,
  blockTreeSeries,
  blockTreeWithTasks
} from './mockData';
import { recurseBlocks } from '../../../utils/recurseBlocks';

export default {
  title: 'Components/Block',
  blockComponent: Block,
  argTypes: {},
  decorators: [(Story) => <Storybook.Wrapper><Story /></Storybook.Wrapper>],
  parameters: {
    badges: [BADGE.DEV]
  }
};

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

const Template = (args) => <Block {...args} />;

export const Basic = Template.bind({});
Basic.args = {
  ...block,
};

export const Editing = Template.bind({});
Editing.args = {
  ...block,
  isEditing: true,
};

export const BlockTree = () => {
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

export const Series = () => {
  const [blockState, setBlockState] = React.useState(blockTreeSeries);
  return recurseBlocks({
    tree: blockState.tree,
    content: blockState.blocks,
    setBlockState: setBlockState,
    ApplyProps: (block) => ({
      ...block,
      handlePressToggle: () => {
        toggleBlockOpen(block, setBlockState)
      }
    }),
    blockComponent: <Block />
  })
}

const BlocksContainer = ({ blocks }) => {
  const [blockState, setBlockState] = React.useState(blocks);
  const presence: PersonPresence[] = mockPeople.map((p, index) => ({ ...p, uid: index.toString() }));

  const returnBlocks = recurseBlocks({
    tree: blockState.tree,
    content: blockState.blocks,
    setBlockState: setBlockState,
    ApplyProps: (block) => ({
      ...block,
      presentUser: presence.find((p: PersonPresence) => p.uid === block.uid),
      handlePressToggle: (uid: UID) => {
        toggleBlockOpen(uid, setBlockState)
      }
    }),
    blockComponent: <Block />
  })
  return returnBlocks;
};

export const WithPresence = () => {
  return <BlocksContainer blocks={blockTree} />
}

// export const WithTasks = () => {
//   const [blockState, setBlockState] = React.useState(blockTreeWithTasks);
//   return
// }

export const References = Template.bind({});
References.args = {
  ...block,
  refsCount: 12
};

export const Selected = Template.bind({});
Selected.args = {
  ...block,
  isSelected: true,
};

export const MultipleSelected = () => {
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

export const ChecklistBlock = () => {
  return <Block
    uid="123"
    rawContent=""
    renderedContent={<Checklist />}
    isOpen={true}
    isEditable={false}
  />
}