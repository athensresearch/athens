import { mockPeople } from '../Avatar/mockData';

export const block = {
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
    }
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
    }
  }
};

export const blockTreeWithSelected = () => ({
  ...blockTree,
  blocks: Object.keys(blockTree.blocks).map(block => ({
    ...blockTree.blocks[block],
    isSelected: true
  })),
})

export const blockTreeWithAvatars = () => ({
  ...blockTree,
  blocks: Object.keys(blockTree.blocks).map(block => ({
    ...blockTree.blocks[block],
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

export const blocks = [
  {
    uid: "1",
    isOpen: true,
    rawContent: "**Lorem** ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
    renderedContent: <><strong>Lorem</strong> ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.</>
  },
  {
    uid: "2",
    isOpen: true,
    rawContent: "**Donec** id elit non mi porta gravida at eget metus. Maecenas sed diam eget risus varius blandit sit amet non magna. Cras justo odio, dapibus ac facilisis in, egestas eget quam.",
    renderedContent: <><strong>Donec</strong> id elit non mi porta gravida at eget metus. Maecenas sed diam eget risus varius blandit sit amet non magna. Cras justo odio, dapibus ac facilisis in, egestas eget quam.</>
  },
  {
    uid: "3",
    isOpen: true,
    rawContent: "**Consectetur** adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
    renderedContent: <><strong>Consectetur</strong> adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.</>
  },
];

export const welcome = [
  {
    uid: "1",
    isOpen: true,
    rawContent: "You can open and close blocks that have children.",
    renderedContent: <>You can open and close blocks that have children.</>
  },
  {
    uid: "2",
    isOpen: true,
    rawContent: "**How to use Athens**",
    renderedContent: <><strong>How to use Athens</strong></>
  },
  {
    uid: "3",
    isOpen: false,
    rawContent: "Outliner Features",
    renderedContent: <>Outliner Features</>
  },
  {
    uid: "4",
    isOpen: false,
    rawContent: "Markup Features",
    renderedContent: <>Outliner features</>
  },
  {
    uid: "5",
    isOpen: false,
    rawContent: "All Keybindings",
    renderedContent: <>All Keybindings</>
  },
]