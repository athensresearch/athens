import React from 'react';

import { Block } from './Block';
import { BADGE, Storybook } from '../../storybook';
import { Checklist } from '../Checkbox/Checkbox.stories';
import { blocks, welcome } from './mockData';

export default {
  title: 'Components/Block',
  component: Block,
  argTypes: {},
  decorators: [(Story) => <Storybook.Wrapper><Story /></Storybook.Wrapper>],
  parameters: {
    badges: [BADGE.DEV]
  }
};

const toggleBlockOpen = (blockId, setBlockState) => setBlockState(prevState => ({ ...prevState, [blockId]: { ...prevState[blockId], isOpen: !prevState[blockId].isOpen } }))

// Stories

const Template = (args) => <Block {...args} />;


export const Basic = Template.bind({});
Basic.args = {
  ...blocks[0],
  refsCount: 12
};

export const Editing = Template.bind({});
Editing.args = {
  ...blocks[0],
  isEditing: true,
};

export const Series = () => {
  const [blockState, setBlockState] = React.useState({
    b0: {
      ...blocks[0],
      handlePressToggle: () => toggleBlockOpen('b0', setBlockState),
      isOpen: true,
    },
    b1: {
      ...blocks[1],
      handlePressToggle: () => toggleBlockOpen('b1', setBlockState),
      isOpen: true,
    },
    b2: {
      ...blocks[2],
      handlePressToggle: () => toggleBlockOpen('b2', setBlockState),
      isOpen: true,
    },
  });

  return (
    <>
      <Block {...blockState.b0} />
      <Block {...blockState.b1} />
      <Block {...blockState.b2} />
    </>
  )
};

export const References = () => {
  return (
    <Block {...blocks[0]} refsCount={12} />
  )
};

export const Selected = () => {
  return (
    <Block {...blocks[0]} isSelected={true} />
  )
};

export const MultipleSelected = () => {
  return (
    <>
      <Block
        {...blocks[0]}
        isSelected={true}
      />
      <Block
        {...blocks[1]}
        isSelected={true}
      >
        <Block
          {...blocks[2]}
          isSelected={true}
        >
          <Block
            {...blocks[3]}
            isSelected={true}
          >
          </Block>
        </Block>
      </Block>
    </>
  )
};

export const Tree = () => {
  const [blockState, setBlockState] = React.useState({
    b0: {
      ...blocks[0],
      handlePressToggle: () => toggleBlockOpen('b0', setBlockState),
      isOpen: true,
    },
    b1: {
      ...blocks[1],
      handlePressToggle: () => toggleBlockOpen('b1', setBlockState),
      isOpen: true,
    },
    b2: {
      ...blocks[2],
      handlePressToggle: () => toggleBlockOpen('b2', setBlockState),
      isOpen: true,
    },
  });

  return (
    <>
      <Block {...blockState.b0}>
        <Block {...blockState.b1}>
          <Block {...blockState.b2} />
        </Block>
      </Block>
    </>
  )
};

export const Welcome = () => {
  const [blockState, setBlockState] = React.useState({
    b0: {
      ...welcome[0],
      handlePressToggle: () => toggleBlockOpen('b0', setBlockState),
    },
    b1: {
      ...welcome[1],
      handlePressToggle: () => toggleBlockOpen('b1', setBlockState),
      isOpen: true,
    },
    b2: {
      ...welcome[2],
      handlePressToggle: () => toggleBlockOpen('b2', setBlockState),
    },
    b3: {
      ...welcome[3],
      handlePressToggle: () => toggleBlockOpen('b3', setBlockState),
    },
    b4: {
      ...welcome[4],
      handlePressToggle: () => toggleBlockOpen('b4', setBlockState),
    },
    b5: {
      ...welcome[5],
      handlePressToggle: () => toggleBlockOpen('b5', setBlockState),
    },
  });

  return (
    <>
      <Block {...blockState.b0} />
      <Block {...blockState.b1} />
      <Block {...blockState.b2} />
      <Block {...blockState.b3} />
      <Block {...blockState.b4} />
      <Block {...blockState.b4} />
    </>
  )
};

export const WithAvatars = ({ ...args }) => {
  const [blockState, setBlockState] = React.useState({
    b0: {
      ...welcome[0],
      handlePressToggle: () => toggleBlockOpen('b0', setBlockState),
      presentUser: { personId: '0', username: "Jeff", color: "#DDA74C" }
    },
    b1: {
      ...welcome[1],
      handlePressToggle: () => toggleBlockOpen('b1', setBlockState),
      presentUser: { personId: '1', username: "Sid", color: "#C45042" },
      refsCount: 12
    },
    b2: {
      ...welcome[2],
      handlePressToggle: () => toggleBlockOpen('b2', setBlockState),
      presentUser: { personId: '2', username: "Matei", color: "#C45042" },
    },
    b3: {
      ...welcome[3],
      handlePressToggle: () => toggleBlockOpen('b3', setBlockState),
      presentUser: { personId: '3', username: "Alex", color: "#611A58" }
    },
    b4: {
      ...welcome[4],
      handlePressToggle: () => toggleBlockOpen('b4', setBlockState),
      presentUser: { personId: '4', username: "Felipe Silva", color: "#21A469" }
    },
    b5: {
      ...welcome[5],
      handlePressToggle: () => toggleBlockOpen('b5', setBlockState),
    },
  });


  return (
    <>
      <Block {...blockState.b0}>
        <Block {...blockState.b1}>
          <Block {...blockState.b2} refsCount={10} />
          <Block {...blockState.b3} refsCount={10} >
            <Block {...blockState.b4} isLocked={true} >
              <Block {...blockState.b5} />
            </Block>
          </Block>
        </Block>
      </Block>
    </>
  )
};

export const ChecklistBlock = () => {
  return <Block
    uid="123"
    rawContent=""
    renderedContent={<Checklist />}
    isOpen={true}
    isEditable={false}
  />
}