import React from 'react';
import styled from 'styled-components';
import { BADGE, Storybook } from '../../storybook';
import * as mockData from './mockData';

import { AppToolbar } from './AppToolbar';

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
    isThemeDark={isThemeDark}
    hostAddress={mockData.hostAddress}
    currentPageMembers={mockData.currentPageMembers}
    differentPageMembers={mockData.differentPageMembers}
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
    handlePressMember={(member) => console.log(member)}
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
