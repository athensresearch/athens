import React from 'react';
import styled from 'styled-components';
import { BADGE, Storybook } from '../../storybook';

import { AppToolbar } from './AppToolbar';

const demoUsers = [{
  username: "crechert0",
  color: "#ba0f68",
  uid: "9f8971fc-df85-4f34-a96b-cb12bd1d6c0e"
}, {
  username: "rgreatorex1",
  color: "#bad554",
  uid: "872e3ff3-1f4b-4f19-aefd-f08a6ca5fc59"
}, {
  username: "dmcnamara2",
  color: "#a39562",
  uid: "82051dcd-18f5-4e1f-be0e-382206190d78"
}, {
  username: "iolech3",
  color: "#faabd4",
  uid: "00d5e9a0-c644-4223-bfec-da78d3b64f25"
}, {
  username: "estannering4",
  color: "#b06f50",
  uid: "f7a3058f-2512-4c43-8f04-4dd732c4289e"
}, {
  username: "mmaffezzoli5",
  color: "#e59ab6",
  uid: "97d0d9d1-9f0e-4573-8711-6992e2f1bab7"
}, {
  username: "dmclae6",
  color: "#105893",
  uid: "9fc60ceb-bd78-4389-8cde-b817fbb76fee"
}, {
  username: "adelahay7",
  color: "#1c7ecc",
  uid: "3bb11a08-a953-4722-ac0b-2e2c3efb4f2b"
}, {
  username: "gbodsworth8",
  color: "#9c1cce",
  uid: "762a5acb-73b4-48c4-a864-b1e3f2799837"
}, {
  username: "ggallgher9",
  color: "#3d6a03",
  uid: "af0ab49d-834d-463e-a4cc-504bb4cb1ac7"
}, {
  username: "phayhowa",
  color: "#09fc6c",
  uid: "c7d55c73-4f69-4ba8-953d-dbdaab3cc142"
}, {
  username: "dwhenhamb",
  color: "#e0145a",
  uid: "08c02851-d7fe-4ed9-b0db-f4b3bc952f16"
}]

const ToolbarStoryWrapper = styled(Storybook.Wrapper)`
  background: rebeccapurple;

  > * {
    /* Make the macOS toolbar behave inside the story */
    position: static !important;
  }
`;

export default {
  title: 'Sections/AppToolbar',
  component: AppToolbar,
  argTypes: {},
  parameters: {
    badges: [BADGE.DEV]
  },
  decorators: [(Story) => <ToolbarStoryWrapper>{Story()}</ToolbarStoryWrapper>]
};

const Template = (args) => {
  const [route, setRoute] = React.useState('');
  const [isWinFullscreen, setIsWinFullscreen] = React.useState(false);
  const [isWinFocused, setIsWinFocused] = React.useState(true);
  const [isWinMaximized, setIsWinMaximized] = React.useState(true);
  const [isLeftSidebarOpen, setIsLeftOpen] = React.useState(false);
  const [isRightSidebarOpen, setIsRightSidebarOpen] = React.useState(false);
  const [isCommandBarOpen, setIsCommandBarOpen] = React.useState(false);
  const [isMergeDialogOpen, setIsMergeDialogOpen] = React.useState(false);
  const [isDatabaseDialogOpen, setIsDatabaseDialogOpen] = React.useState(false);
  const [isThemeDark, setIsThemeDark] = React.useState(false);

  return <AppToolbar
    route={route}
    isWinFullscreen={isWinFullscreen}
    isWinFocused={isWinFocused}
    isWinMaximized={isWinMaximized}
    isLeftSidebarOpen={isLeftSidebarOpen}
    isRightSidebarOpen={isRightSidebarOpen}
    isCommandBarOpen={isCommandBarOpen}
    isMergeDialogOpen={isMergeDialogOpen}
    isDatabaseDialogOpen={isDatabaseDialogOpen}
    hostAddress="192.168.0.1"
    isThemeDark={isThemeDark}
    currentPageMembers={demoUsers.slice(0, 5)}
    differentPageMembers={demoUsers.slice(5, 10)}
    handlePessLeftButton={() => setIsLeftOpen(!isLeftSidebarOpen)}
    handlePressCommandBar={() => setIsCommandBarOpen(!isCommandBarOpen)}
    handlePressDailyNotes={() => setRoute('/daily-notes')}
    handlePressAllPages={() => setRoute('/all-pages')}
    handlePressGraph={() => setRoute('/graph')}
    handlePressThemeToggle={() => setIsThemeDark(!isThemeDark)}
    handlePressMerge={() => setIsMergeDialogOpen(!isMergeDialogOpen)}
    handlePressSettings={() => setRoute('/settings')}
    handlePressDatabase={() => setIsDatabaseDialogOpen(!isDatabaseDialogOpen)}
    handlePressShortcuts={() => setIsLeftOpen(!isLeftSidebarOpen)}
    handlePressRightSidebar={() => setIsRightSidebarOpen(!isRightSidebarOpen)}
    {...args} />
};

export const Auto = Template.bind({});
Auto.args = {};

export const MacOs = Template.bind({});
MacOs.args = {
  os: 'mac',
  isElectron: true,
};

export const Windows = Template.bind({});
Windows.args = {
  os: 'windows',
  isElectron: true
};

export const Linux = Template.bind({});
Linux.args = {
  os: 'linux',
  isElectron: true
};
