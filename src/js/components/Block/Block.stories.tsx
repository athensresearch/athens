import React from 'react';
import { Block } from './Block';

import styled from 'styled-components';

const Wrapper = styled.div`
  padding: 4rem;
`;

const data = {
  blockContent: [
    {
      raw: "**Lorem** ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
      rendered: () => <><strong>Lorem</strong> ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.</>
    },
    {
      raw: "**Donec** id elit non mi porta gravida at eget metus. Maecenas sed diam eget risus varius blandit sit amet non magna. Cras justo odio, dapibus ac facilisis in, egestas eget quam.",
      rendered: () => <><strong>Donec</strong> id elit non mi porta gravida at eget metus. Maecenas sed diam eget risus varius blandit sit amet non magna. Cras justo odio, dapibus ac facilisis in, egestas eget quam.</>
    },
    {
      raw: "**Consectetur** adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
      rendered: () => <><strong>Consectetur</strong> adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.</>
    },
  ]
};

export default {
  title: 'Components/Block',
  component: Block,
  argTypes: {},
};

// Stories

const Template = (args) => <Wrapper><Block {...args} /></Wrapper>;

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

  const toggleBlockOpen = (blockId) => setBlockState(prevState => ({ ...prevState, [blockId]: { ...prevState[blockId], isOpen: !prevState[blockId].isOpen } }))

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
Series.decorators = [(Story) => <Wrapper><Story /></Wrapper>];

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
References.decorators = [(Story) => <Wrapper><Story /></Wrapper>];

export const Selected = () => {
  return (
    <Wrapper>
      <Block
        uid="1"
        isSelected={true}
        isOpen={true}
        rawContent={data.blockContent[0].raw}
        renderedContent={data.blockContent[0].rendered()}
      >
      </Block>
    </Wrapper>
  )
};
Selected.decorators = [(Story) => <Wrapper><Story /></Wrapper>];

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
MultipleSelected.decorators = [(Story) => <Wrapper><Story /></Wrapper>];

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

  const toggleBlockOpen = (blockId) => setBlockState(prevState => ({ ...prevState, [blockId]: { ...prevState[blockId], isOpen: !prevState[blockId].isOpen } }))

  return (
    <>
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
    </>
  )
};
Tree.decorators = [(Story) => <Wrapper><Story /></Wrapper>];

export const Welcome = () => {
  const [blockState, setBlockState] = React.useState({
    b1: {
      isOpen: true,
      rawContent: data.blockContent[1].raw,
      renderedContent: data.blockContent[1].rendered(),
    },
    b2: {
      isOpen: true,
      rawContent: data.blockContent[1].raw,
      renderedContent: data.blockContent[1].rendered(),
    },
    b3: {
      isOpen: true,
      rawContent: data.blockContent[1].raw,
      renderedContent: data.blockContent[1].rendered(),
    },
    b4: {
      isOpen: true,
      rawContent: data.blockContent[1].raw,
      renderedContent: data.blockContent[1].rendered(),
    },
    b5: {
      isOpen: true,
      rawContent: data.blockContent[1].raw,
      renderedContent: data.blockContent[1].rendered(),
    },
    b6: {
      isOpen: true,
      rawContent: data.blockContent[1].raw,
      renderedContent: data.blockContent[1].rendered(),
    },
  })

  const toggleBlockOpen = (blockId) => setBlockState(prevState => ({ ...prevState, [blockId]: { ...prevState[blockId], isOpen: !prevState[blockId].isOpen } }))

  return (
    <>
      <Block
        uid="1"
        isOpen={blockState["b1"].isOpen}
        handlePressToggle={() => toggleBlockOpen("b1")}
        rawContent={blockState["b1"].rawContent}
        renderedContent={blockState["b1"].renderedContent}
      >
        <Block uid="2"
          isOpen={blockState["b2"].isOpen}
          handlePressToggle={() => toggleBlockOpen("b2")}
          rawContent={blockState["b2"].rawContent}
          renderedContent={blockState["b2"].renderedContent}
        >
          <Block uid="3"
            isOpen={blockState["b3"].isOpen}
            handlePressToggle={() => toggleBlockOpen("b3")}
            rawContent={blockState["b3"].rawContent}
            renderedContent={blockState["b3"].renderedContent}
          />
          <Block
            uid="4"
            isOpen={blockState["b4"].isOpen}
            handlePressToggle={() => toggleBlockOpen("b4")}
            rawContent={blockState["b4"].rawContent}
            renderedContent={blockState["b4"].renderedContent}
          >
            <Block uid="5"
              isOpen={blockState["b5"].isOpen}
              handlePressToggle={() => toggleBlockOpen("b5")}
              rawContent={blockState["b5"].rawContent}
              renderedContent={blockState["b5"].renderedContent}
            >
              <Block uid="6"
                isOpen={blockState["b6"].isOpen}
                handlePressToggle={() => toggleBlockOpen("b6")}
                rawContent={blockState["b6"].rawContent}
                renderedContent={blockState["b6"].renderedContent}
              />
            </Block>
          </Block>
        </Block>
      </Block>
    </>
  )
};
Welcome.decorators = [(Story) => <Wrapper><Story /></Wrapper>];

export const WithAvatars = ({ ...args }) => {
  const [blockState, setBlockState] = React.useState({
    b1: {
      isOpen: true,
      rawContent: data.blockContent[1].raw,
      renderedContent: data.blockContent[1].rendered(),
      presentUser: { name: "Jeff", color: args.color },
    },
    b2: {
      isOpen: true,
      rawContent: data.blockContent[1].raw,
      renderedContent: data.blockContent[1].rendered(),
      presentUser: { name: "Matei", color: args.color },
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
      presentUser: { name: "Alex", color: args.color },
    },
    b5: {
      isOpen: true,
      rawContent: data.blockContent[1].raw,
      renderedContent: data.blockContent[1].rendered(),
      presentUser: { name: "Felipe", color: args.color },
    },
    b6: {
      isOpen: true,
      rawContent: data.blockContent[1].raw,
      renderedContent: data.blockContent[1].rendered(),
      presentUser: null,
    },
  })

  const toggleBlockOpen = (blockId) => setBlockState(prevState => ({ ...prevState, [blockId]: { ...prevState[blockId], isOpen: !prevState[blockId].isOpen } }))

  return (
    <>
      <Block
        uid="1"
        isOpen={blockState["b1"].isOpen}
        handlePressToggle={() => toggleBlockOpen("b1")}
        rawContent={blockState["b1"].rawContent}
        renderedContent={blockState["b1"].renderedContent}
        presentUser={{ name: "Jeff", color: args.color || "#DDA74C" }}
      >
        <Block uid="2"
          isOpen={blockState["b2"].isOpen}
          handlePressToggle={() => toggleBlockOpen("b2")}
          rawContent={blockState["b2"].rawContent}
          renderedContent={blockState["b2"].renderedContent}
          presentUser={{ name: "Matei", color: args.color || "#C45042" }}
        >
          <Block uid="3"
            isOpen={blockState["b3"].isOpen}
            handlePressToggle={() => toggleBlockOpen("b3")}
            rawContent={blockState["b3"].rawContent}
            renderedContent={blockState["b3"].renderedContent}
          />
          <Block
            uid="4"
            isOpen={blockState["b4"].isOpen}
            handlePressToggle={() => toggleBlockOpen("b4")}
            rawContent={blockState["b4"].rawContent}
            renderedContent={blockState["b4"].renderedContent}
            presentUser={{ name: "Alex", color: args.color || "#611A58" }}
          >
            <Block uid="5"
              isOpen={blockState["b5"].isOpen}
              handlePressToggle={() => toggleBlockOpen("b5")}
              rawContent={blockState["b5"].rawContent}
              renderedContent={blockState["b5"].renderedContent}
              presentUser={{ name: "Felipe", color: args.color || "#21A469" }}
            >
              <Block uid="6"
                isOpen={blockState["b6"].isOpen}
                handlePressToggle={() => toggleBlockOpen("b6")}
                rawContent={blockState["b6"].rawContent}
                renderedContent={blockState["b6"].renderedContent}
                presentUser={null}
              />
            </Block>
          </Block>
        </Block>
      </Block>
    </>
  )
};
WithAvatars.args = {
  color: "#611A58",
}
WithAvatars.decorators = [(Story) => <Wrapper><Story /></Wrapper>];