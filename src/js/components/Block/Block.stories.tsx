import React from 'react';
import { Block } from './Block';

// Helpers

const data = {
  blockContent: [
    {
      raw: "**Lorem** ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
      rendered: () => <><strong>Lorem</strong> ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.</>
    },
    {
      raw: "**Donec** id elit non mi porta gravida at eget metus. Maecenas sed diam eget risus varius blandit sit amet non magna. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at eget metus. Nullam id dolor id nibh ultricies vehicula ut id elit. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at eget metus. Nullam id dolor id nibh ultricies vehicula ut id elit. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at eget metus. Nullam id dolor id nibh ultricies vehicula ut id elit. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at eget metus. Nullam id dolor id nibh ultricies vehicula ut id elit. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at eget metus. Nullam id dolor id nibh ultricies vehicula ut id elit. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at eget metus. Nullam id dolor id nibh ultricies vehicula ut id elit. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at eget metus. Nullam id dolor id nibh ultricies vehicula ut id elit. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at.",
      rendered: () => <><strong>Donec</strong> id elit non mi porta gravida at eget metus. Maecenas sed diam eget risus varius blandit sit amet non magna. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at eget metus. Nullam id dolor id nibh ultricies vehicula ut id elit. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at eget metus. Nullam id dolor id nibh ultricies vehicula ut id elit. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at eget metus. Nullam id dolor id nibh ultricies vehicula ut id elit. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at eget metus. Nullam id dolor id nibh ultricies vehicula ut id elit. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at eget metus. Nullam id dolor id nibh ultricies vehicula ut id elit. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at eget metus. Nullam id dolor id nibh ultricies vehicula ut id elit. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at eget metus. Nullam id dolor id nibh ultricies vehicula ut id elit. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at.</>
    },
    {
      raw: "**Consectetur** adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
      rendered: () => <><strong>Consectetur</strong> adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.</>
    },
  ]
};

export default {
  title: 'Components/Block',
  component: Block,
  argTypes: {},
};

// Stories

const Template = (args) => <Block {...args} />;

export const Basic = Template.bind({});
Basic.args = {
  uid: "123",
  rawContent: data.blockContent[0].raw,
  renderedContent: data.blockContent[0].rendered(),
  isOpen: true,
  refsCount: 12
};

export const Editing = Template.bind({});
Editing.args = {
  isEditing: true,
  isOpen: true,
  rawContent: data.blockContent[0].raw,
  renderedContent: data.blockContent[0].rendered(),
};

export const Series = () => {
  const [blockState, setBlockState] = React.useState({
    b1: {
      isOpen: true,
      rawContent: data.blockContent[0].raw,
      renderedContent: data.blockContent[0].rendered(),
      presentUser: null,
    },
    b2: {
      isOpen: true,
      rawContent: data.blockContent[1].raw,
      renderedContent: data.blockContent[1].rendered(),
      presentUser: null,
    },
    b3: {
      isOpen: true,
      rawContent: data.blockContent[2].raw,
      renderedContent: data.blockContent[2].rendered(),
      presentUser: null,
    },
  });

  const toggleBlockOpen = (blockId) => setBlockState(prevState => ({ ...prevState, [blockId]: { isOpen: !prevState[blockId].isOpen } }))

  return (
    <>
      <Block
        uid="1"
        isOpen={blockState["b1"].isOpen}
        handlePressToggle={() => toggleBlockOpen("b1")}
        rawContent={blockState["b1"].rawContent}
        renderedContent={blockState["b1"].renderedContent}
        presentUser={blockState["b1"].presentUser}
    />
      <Block
        uid="2"
        isOpen={blockState["b2"].isOpen}
        handlePressToggle={() => toggleBlockOpen("b2")}
        rawContent={blockState["b2"].rawContent}
        renderedContent={blockState["b2"].renderedContent}
        presentUser={blockState["b2"].presentUser}
    />
      <Block uid="3"
        isOpen={blockState["b3"].isOpen}
        handlePressToggle={() => toggleBlockOpen("b3")}
        rawContent={blockState["b3"].rawContent}
        renderedContent={blockState["b3"].renderedContent}
        presentUser={blockState["b3"].presentUser}
      />
    </>
  )
};

export const References = () => {
  return (
    <Block
      uid="1"
      isSelected={false}
      isOpen={true}
      refsCount={12}
      rawContent={data.blockContent[0].raw}
      renderedContent={data.blockContent[0].rendered()}
    >
    </Block>
  )
};

export const Selected = () => {
  return (
    <Block
      uid="1"
      isSelected={true}
      isOpen={true}
      rawContent={data.blockContent[0].raw}
      renderedContent={data.blockContent[0].rendered()}
    >
    </Block>
  )
};

export const MultipleSelected = () => {
  return (
    <>
    <Block
      uid="123"
      isSelected={true}
      isOpen={true}
        rawContent={data.blockContent[0].raw}
        renderedContent={data.blockContent[0].rendered()}
    />
    <Block
      uid="123"
      isSelected={true}
      isOpen={true}
        rawContent={data.blockContent[1].raw}
        renderedContent={data.blockContent[1].rendered()}
    >
      <Block
        uid="123"
        isSelected={true}
        isOpen={true}
          rawContent={data.blockContent[2].raw}
          renderedContent={data.blockContent[2].rendered()}
      >
        <Block
          uid="123"
          isSelected={true}
          isOpen={true}
            rawContent={data.blockContent[0].raw}
            renderedContent={data.blockContent[0].rendered()}
        >
        </Block>
      </Block>
      </Block>
    </>
  )
};

export const Tree = () => {
  const [blockState, setBlockState] = React.useState({
    b1: {
      isOpen: true,
      rawContent: data.blockContent[0].raw,
      renderedContent: data.blockContent[0].rendered(),
      presentUser: null,
    },
    b2: {
      isOpen: true,
      rawContent: data.blockContent[1].raw,
      renderedContent: data.blockContent[1].rendered(),
      presentUser: null,
    },
    b3: {
      isOpen: true,
      rawContent: data.blockContent[2].raw,
      renderedContent: data.blockContent[2].rendered(),
      presentUser: null,
    },
  });

  const toggleBlockOpen = (blockId) => setBlockState(prevState => ({ ...prevState, [blockId]: { isOpen: !prevState[blockId].isOpen } }))

  return (
    <Block
      uid="1"
      isOpen={blockState["b1"].isOpen}
      handlePressToggle={() => toggleBlockOpen("b1")}
      rawContent={blockState["b1"].rawContent}
      renderedContent={blockState["b1"].renderedContent}
      presentUser={blockState["b1"].presentUser}
    >
      <Block uid="2"
        isOpen={blockState["b2"].isOpen}
        handlePressToggle={() => toggleBlockOpen("b2")}
        rawContent={blockState["b2"].rawContent}
        renderedContent={blockState["b2"].renderedContent}
        presentUser={blockState["b2"].presentUser}
      >
        <Block uid="3"
          isOpen={blockState["b3"].isOpen}
          handlePressToggle={() => toggleBlockOpen("b3")}
          rawContent={blockState["b3"].rawContent}
          renderedContent={blockState["b3"].renderedContent}
          presentUser={blockState["b3"].presentUser}
        />
      </Block>
    </Block>
  )
};

export const Welcome = () => {
  const [blockState, setBlockState] = React.useState({
    b1: {
      isOpen: true,
      rawContent: data.blockContent[1].raw,
      renderedContent: data.blockContent[1].rendered(),
      presentUser: null,
    },
    b2: {
      isOpen: true,
      rawContent: data.blockContent[1].raw,
      renderedContent: data.blockContent[1].rendered(),
      presentUser: { name: "Jeff", color: "blue" },
    },
    b3: {
      isOpen: true,
      rawContent: data.blockContent[1].raw,
      renderedContent: data.blockContent[1].rendered(),
      presentUser: null,
    },
    b4: {
      isOpen: true,
      rawContent: data.blockContent[1].raw,
      renderedContent: data.blockContent[1].rendered(),
      presentUser: null,
    },
    b5: {
      isOpen: true,
      rawContent: data.blockContent[1].raw,
      renderedContent: data.blockContent[1].rendered(),
      presentUser: null,
    },
    b6: {
      isOpen: true,
      rawContent: data.blockContent[1].raw,
      renderedContent: data.blockContent[1].rendered(),
      presentUser: null,
    },
  })

  const toggleBlockOpen = (blockId) => setBlockState(prevState => ({ ...prevState, [blockId]: { isOpen: !prevState[blockId].isOpen } }))

  return (
    <Block
      uid="1"
      isOpen={blockState["b1"].isOpen}
      handlePressToggle={() => toggleBlockOpen("b1")}
      rawContent={blockState["b1"].rawContent}
      renderedContent={blockState["b1"].renderedContent}
      presentUser={blockState["b1"].presentUser}
    >
      <Block uid="2"
        isOpen={blockState["b2"].isOpen}
        handlePressToggle={() => toggleBlockOpen("b2")}
        rawContent={blockState["b2"].rawContent}
        renderedContent={blockState["b2"].renderedContent}
        presentUser={blockState["b2"].presentUser}
      >
        <Block uid="3"
          isOpen={blockState["b3"].isOpen}
          handlePressToggle={() => toggleBlockOpen("b3")}
          rawContent={blockState["b3"].rawContent}
          renderedContent={blockState["b3"].renderedContent}
          presentUser={blockState["b3"].presentUser}
        />
        <Block
          uid="4"
          isOpen={blockState["b4"].isOpen}
          handlePressToggle={() => toggleBlockOpen("b4")}
          rawContent={blockState["b4"].rawContent}
          renderedContent={blockState["b4"].renderedContent}
          presentUser={blockState["b4"].presentUser}
        >
          <Block uid="5"
            isOpen={blockState["b5"].isOpen}
            handlePressToggle={() => toggleBlockOpen("b5")}
            rawContent={blockState["b5"].rawContent}
            renderedContent={blockState["b5"].renderedContent}
            presentUser={blockState["b5"].presentUser}
          >
            <Block uid="6"
              isOpen={blockState["b6"].isOpen}
              handlePressToggle={() => toggleBlockOpen("b6")}
              rawContent={blockState["b6"].rawContent}
              renderedContent={blockState["b6"].renderedContent}
              presentUser={blockState["b6"].presentUser}
            />
          </Block>
        </Block>
      </Block>
    </Block>
  )
};
