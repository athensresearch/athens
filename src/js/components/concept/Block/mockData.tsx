import { mockPeople } from '../../Avatar/mockData';
import { Checkbox } from '../Checkbox';

export const block = {
  uid: '1',
  isOpen: true,
  rawContent: "**Donec** id elit non mi porta gravida at eget metus. Maecenas sed diam eget risus varius blandit sit amet non magna. Cras justo odio, dapibus ac facilisis in, egestas eget quam.",
  renderedContent: <><strong>Donec</strong> id elit non mi porta gravida at eget metus. Maecenas sed diam eget risus varius blandit sit amet non magna. Cras justo odio, dapibus ac facilisis in, egestas eget quam.</>,
}

export const blockTree = {
  tree: [
    {
      uid: "1",
      children: [
        {
          uid: "2",
          children: [{
            uid: "3",
          }],
        },
        {
          uid: "4",
          children: [{
            uid: "5",
          }],
        },
      ],
    }, { uid: "6" }
  ],
  blocks: {
    "1": {
      isOpen: true,
      rawContent: "**Lorem** ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
      renderedContent: <><strong>Lorem</strong> ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.</>,
    },
    "2": {
      isOpen: true,
      rawContent: "**Donec** id elit non mi porta gravida at eget metus. Maecenas sed diam eget risus varius blandit sit amet non magna. Cras justo odio, dapibus ac facilisis in, egestas eget quam.",
      renderedContent: <><strong>Donec</strong> id elit non mi porta gravida at eget metus. Maecenas sed diam eget risus varius blandit sit amet non magna. Cras justo odio, dapibus ac facilisis in, egestas eget quam.</>,
    },
    "3": {
      isOpen: true,
      rawContent: "**Consectetur** adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
      renderedContent: <><strong>Consectetur</strong> adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.</>
    },
    "4": {
      isOpen: true,
      rawContent: "**Donec** id elit non mi porta gravida at eget metus. Maecenas sed diam eget risus varius blandit sit amet non magna. Cras justo odio, dapibus ac facilisis in, egestas eget quam.",
      renderedContent: <><strong>Donec</strong> id elit non mi porta gravida at eget metus. Maecenas sed diam eget risus varius blandit sit amet non magna. Cras justo odio, dapibus ac facilisis in, egestas eget quam.</>,
    },
    "5": {
      isOpen: true,
      rawContent: "**Consectetur** adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
      renderedContent: <><strong>Consectetur</strong> adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.</>
    },
    "6": {
      isOpen: true,
      rawContent: "**Consectetur** adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
      renderedContent: <><strong>Consectetur</strong> adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.</>
    }
  }
};

export const blockTreeWithTasks = () => {
  return ({
    ...blockTree,
    blocks: Object.keys(blockTree.blocks).map(uid => ({
      ...blockTree.blocks[uid],
      renderedContent: (<Checkbox
        styleCircle>
        {blockTree.blocks[uid].renderedContent}</Checkbox>),
    })),
  })
}


export const blockTreeWithAvatars = () => ({
  ...blockTree,
  blocks: Object.keys(blockTree.blocks).map(uid => ({
    ...blockTree.blocks[uid],
    presentUser: mockPeople[Math.random() * mockPeople.length | 0]
  })),
})

export const blockTreeSeries = {
  tree: [
    {
      uid: "1"
    },
    {
      uid: "2"
    },
    {
      uid: "3"
    },
  ],
  blocks: {
    "1": {
      isOpen: true,
      rawContent: "**Lorem** ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
      renderedContent: <><strong>Lorem</strong> ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.</>,
    },
    "2": {
      isOpen: true,
      rawContent: "**Donec** id elit non mi porta gravida at eget metus. Maecenas sed diam eget risus varius blandit sit amet non magna. Cras justo odio, dapibus ac facilisis in, egestas eget quam.",
      renderedContent: <><strong>Donec</strong> id elit non mi porta gravida at eget metus. Maecenas sed diam eget risus varius blandit sit amet non magna. Cras justo odio, dapibus ac facilisis in, egestas eget quam.</>,
    },
    "3": {
      isOpen: true,
      rawContent: "**Consectetur** adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
      renderedContent: <><strong>Consectetur</strong> adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.</>
    },
  }
};
