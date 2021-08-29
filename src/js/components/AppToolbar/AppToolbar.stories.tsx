import React from 'react';

import { AppToolbar } from './AppToolbar';

export default {
  title: 'Components/AppToolbar',
  component: AppToolbar,
  argTypes: {},
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

export const Primary = Template.bind({});
Primary.args = {};

export const MacOs = Template.bind({});
MacOs.args = {
  os: 'mac',
  isElectron: true
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
