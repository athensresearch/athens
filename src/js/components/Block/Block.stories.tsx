import React from 'react';
import { Block } from './Block';
import styled from 'styled-components';

// Helpers

const toggle = (bool, setter) => {
  bool ? setter(!bool) : setter(true);
}

const data = {
  randomBlockContent: [
    "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
    "Donec id elit non mi porta gravida at eget metus. Maecenas sed diam eget risus varius blandit sit amet non magna. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at eget metus. Nullam id dolor id nibh ultricies vehicula ut id elit. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at eget metus. Nullam id dolor id nibh ultricies vehicula ut id elit. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at eget metus. Nullam id dolor id nibh ultricies vehicula ut id elit. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at eget metus. Nullam id dolor id nibh ultricies vehicula ut id elit. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at eget metus. Nullam id dolor id nibh ultricies vehicula ut id elit. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at eget metus. Nullam id dolor id nibh ultricies vehicula ut id elit. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at eget metus. Nullam id dolor id nibh ultricies vehicula ut id elit. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at.",
    "Consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
  ],
  contentWithLink: () => <>regular text <a href="https://www.google.com">link</a> regular text</>,
};

const BlockContainer = styled.div`
  flex: 1 1 100%;
  padding: 1rem;
  max-width: 900px;
`;

export default {
  title: 'Components/Block',
  component: Block,
  argTypes: {},
};

// Stories

const Template = (args) => <Block {...args} />;

export const Typical = Template.bind({});
Typical.args = {
  uid: "123",
  rawContent: data.randomBlockContent[0],
  isOpen: true,
  refsCount: 12
};

export const Editing = Template.bind({});
Editing.args = {
  isEditing: true,
  isOpen: true,
  rawContent: data.randomBlockContent[0]
};

export const Series = () => {
  const [block1Open, setBlock1Open] = React.useState(true);
  const [block2Open, setBlock2Open] = React.useState(true);
  const [block3Open, setBlock3Open] = React.useState(true);

  return (
    <>
    <Block
      uid="123"
      isOpen={block1Open}
      handlePressToggle={() => toggle(block1Open, setBlock1Open)}
      rawContent={data.randomBlockContent[0]}
    />
    <Block uid="123"
      isOpen={block2Open}
      handlePressToggle={() => toggle(block2Open, setBlock2Open)}
      rawContent={data.randomBlockContent[1]}
    />
    <Block uid="123"
      isOpen={block3Open}
      handlePressToggle={() => toggle(block3Open, setBlock3Open)}
      rawContent={data.randomBlockContent[2]}
      />
    </>
  )
};

export const EditableSeries = () => {
  const [block1Content, setBlock1Content] = React.useState(data.randomBlockContent[0]);
  const [block2Content, setBlock2Content] = React.useState(data.randomBlockContent[1]);
  const [block3Content, setBlock3Content] = React.useState(data.randomBlockContent[2]);
  const [editingUid, setEditingUid] = React.useState(null);
  const updateBlockContent = (uid, content) => {
    console.log(uid, content)
    if (uid === "1") {
      setBlock1Content(content);
    } else if (uid === "2") {
      setBlock2Content(content);
    } else if (uid === "3") {
      setBlock3Content(content);
    }
  }

  return (
    <>
    <Block
      uid="1"
      isOpen={true}
      isEditing={editingUid === "1"}
      handlePressContainer={() => setEditingUid("1")}
      handleContentChange={(e) => updateBlockContent("1", e.target.value)}
      rawContent={block1Content}
    />
    <Block
      uid="2"
      isOpen={true}
      isEditing={editingUid === "2"}
      handlePressContainer={() => setEditingUid("2")}
      handleContentChange={(e) => updateBlockContent("2", e.target.value)}
      rawContent={block2Content}
    />
    <Block
      uid="3"
      isOpen={true}
      isEditing={editingUid === "3"}
      handlePressContainer={() => setEditingUid("3")}
      handleContentChange={(e) => updateBlockContent("3", e.target.value)}
      rawContent={block3Content}
      />
    </>
  )
};

export const References = () => {
  return (
    <Block
      uid="123"
      isSelected={true}
      isOpen={true}
      refsCount={12}
      rawContent={data.randomBlockContent[0]}
    >
    </Block>
  )
};

export const Selected = () => {
  return (
    <Block
      uid="123"
      isSelected={true}
      isOpen={true}
      rawContent={data.randomBlockContent[0]}
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
      rawContent={data.randomBlockContent[0]}
    />
    <Block
      uid="123"
      isSelected={true}
      isOpen={true}
      rawContent={data.randomBlockContent[0]}
    >
      <Block
        uid="123"
        isSelected={true}
        isOpen={true}
        rawContent={data.randomBlockContent[1]}
      >
        <Block
          uid="123"
          isSelected={true}
          isOpen={true}
          rawContent={data.randomBlockContent[2]}
        >
        </Block>
      </Block>
      </Block>
    </>
  )
};

export const Tree = () => {
  const [block1Open, setBlock1Open] = React.useState(true);
  const [block2Open, setBlock2Open] = React.useState(true);
  const [block3Open, setBlock3Open] = React.useState(true);

  return (
    <Block
      uid="123"
      isOpen={block1Open}
      handlePressToggle={() => toggle(block1Open, setBlock1Open)}
      rawContent={data.randomBlockContent[0]}
    >
      <Block uid="123"
        isOpen={block2Open}
        handlePressToggle={() => toggle(block2Open, setBlock2Open)}
        rawContent={data.randomBlockContent[1]}
      >
        <Block uid="123"
          isOpen={block3Open}
          handlePressToggle={() => toggle(block3Open, setBlock3Open)}
          rawContent={data.randomBlockContent[2]}
        />
      </Block>
    </Block>
  )
};

export const Welcome = () => {
  const [block1Open, setBlock1Open] = React.useState(true);
  const [block2Open, setBlock2Open] = React.useState(true);
  const [block3Open, setBlock3Open] = React.useState(true);

  return (
    <Block
      uid="123"
      isOpen={block1Open}
      handlePressToggle={() => toggle(block1Open, setBlock1Open)}
      rawContent="asd"
      renderedContent={data.contentWithLink()}
    >
      <Block uid="123"
        isOpen={block2Open}
        handlePressToggle={() => toggle(block2Open, setBlock2Open)}
        rawContent={data.randomBlockContent[1]}
      >
        <Block uid="123"
          isOpen={block3Open}
          handlePressToggle={() => toggle(block3Open, setBlock3Open)}
          rawContent={data.randomBlockContent[2]}
        />
        <Block
          uid="123"
          isOpen={block1Open}
          handlePressToggle={() => toggle(block1Open, setBlock1Open)}
          rawContent="asd"
          renderedContent={data.contentWithLink()}
        >
          <Block uid="123"
            isOpen={block2Open}
            handlePressToggle={() => toggle(block2Open, setBlock2Open)}
            rawContent={data.randomBlockContent[1]}
          >
            <Block uid="123"
              isOpen={block3Open}
              handlePressToggle={() => toggle(block3Open, setBlock3Open)}
              rawContent={data.randomBlockContent[2]}
            />
          </Block>
        </Block>
      </Block>
    </Block>
  )
};
