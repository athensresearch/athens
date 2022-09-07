import React from 'react';
import { BADGE, Storybook } from '@/utils/storybook';
import * as mockPresence from '@/PresenceDetails/mockData';
import { useAppState } from '@/utils/useAppState';

import { AppToolbar, AppToolbarProps } from './AppToolbar';
import { PresenceDetails } from '@/PresenceDetails';


export default {
  title: 'Sections/AppToolbar',
  component: AppToolbar,
  subcomponents: { PresenceDetails },
  argTypes: {},
  parameters: {
    badges: [BADGE.DEV]
  },
};

const Template = (args: AppToolbarProps) => {
  const {
    currentUser,
    setCurrentUser,
    route,
    setRoute,
    isWinFullscreen,
    isWinFocused,
    isWinMaximized,
    isLeftSidebarOpen,
    setIsLeftSidebarOpen,
    isRightSidebarOpen,
    setIsRightSidebarOpen,
    isCommandBarOpen,
    setIsCommandBarOpen,
    isMergeDialogOpen,
    setIsMergeDialogOpen,
    isDatabaseDialogOpen,
    setIsDatabaseDialogOpen,
    isThemeDark,
    setIsThemeDark
  } = useAppState();

  return <AppToolbar
    route={route}
    isWinFullscreen={isWinFullscreen}
    isWinFocused={isWinFocused}
    isWinMaximized={isWinMaximized}
    isLeftSidebarOpen={isLeftSidebarOpen}
    isRightSidebarOpen={isRightSidebarOpen}
    isCommandBarOpen={isCommandBarOpen}
    isThemeDark={isThemeDark}
    hostAddress={mockPresence.hostAddress}
    currentUser={currentUser}
    currentPageMembers={mockPresence.currentPageMembers}
    differentPageMembers={mockPresence.differentPageMembers}
    onPressLeftSidebarToggle={() => setIsLeftSidebarOpen(!isLeftSidebarOpen)}
    onPressCommandBar={() => setIsCommandBarOpen(!isCommandBarOpen)}
    onPressDailyNotes={() => setRoute('/daily-notes')}
    onPressAllPages={() => setRoute('/all-pages')}
    onPressGraph={() => setRoute('/graph')}
    onPressThemeToggle={() => setIsThemeDark(!isThemeDark)}
    onPressMerge={() => setIsMergeDialogOpen(!isMergeDialogOpen)}
    onPressSettings={() => setRoute('/settings')}
    onPressRightSidebarToggle={() => setIsRightSidebarOpen(!isRightSidebarOpen)}
  />
};
// handleChooseDatabase={() => null}
// handlePressAddDatabase={() => null}
// handlePressRemoveDatabase={() => null}
// handlePressImportDatabase={() => null}
// activeDatabase={mockDatabases[0]}
// inactiveDatabases={mockDatabases.slice(1, 4)}
// isSynced={true}
// isMergeDialogOpen={isMergeDialogOpen}
// isDatabaseDialogOpen={isDatabaseDialogOpen}
// onPressMember={(member) => console.log(member)}
// onUpdateProfile={(person) => setCurrentUser(person)}

export const Default = Template.bind({});
Default.args = {
  isElectron: true,
};
Default.decorators = [];

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
