import React from 'react';

import { Block } from './Block';
import { BADGE, Storybook } from '../../storybook';
import { Checklist } from '../Checkbox/Checkbox.stories';
import { block, blockTree, blockTreeSeries, blockTreeWithAvatars } from './mockData';
import { recurseBlocks } from '../../utils/recurseBlocks';

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
    blockProps: {
      handlePressToggle: (uid) => {
        // console.log(blocks);
        // console.log(uid);
        toggleBlockOpen(uid, setBlockState)
      }
    },
    blockComponent: <Block />
  })
}

export const Series = () => {
  const [blockState, setBlockState] = React.useState(blockTreeSeries);
  return recurseBlocks({
    tree: blockState.tree,
    content: blockState.blocks,
    setBlockState: setBlockState,
    blockProps: {
      handlePressToggle: () => toggleBlockOpen(block.uid, setBlockState)
    },
    blockComponent: <Block />
  })
}

export const WithPresence = () => {
  const [blockState, setBlockState] = React.useState(blockTreeWithAvatars);
  return recurseBlocks({
    tree: blockState.tree,
    content: blockState.blocks,
    setBlockState: setBlockState,
    blockProps: {
      handlePressToggle: () => toggleBlockOpen(block.uid, setBlockState)
    },
    blockComponent: <Block />
  })
}

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
    blockProps: {
      isSelected: true,
      handlePressToggle: () => toggleBlockOpen(block.uid, setBlockState)
    },
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