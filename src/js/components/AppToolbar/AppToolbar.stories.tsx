import React from 'react';
import styled from 'styled-components';
import { BADGE, Storybook } from '../../storybook';
import { mockDatabases } from '../DatabaseMenu/mockData';
import * as mockPresence from '../PresenceDetails/mockData';
import { useAppState } from '../../useAppState';

import { AppToolbar, AppToolbarProps } from './AppToolbar';
import { DatabaseMenu } from '../DatabaseMenu';
import { PresenceDetails } from '../PresenceDetails';

const ToolbarStoryWrapper = styled(Storybook.Desktop)`
  > * {
    /* Make the macOS toolbar behave inside the story */
    position: static !important;
    width: 100%;
  }
`;


export default {
  title: 'Sections/AppToolbar',
  component: AppToolbar,
  subcomponents: { DatabaseMenu, PresenceDetails },
  argTypes: {},
  parameters: {
    badges: [BADGE.DEV]
  },
  decorators: [(Story) => <ToolbarStoryWrapper>{Story()}</ToolbarStoryWrapper>]
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
    isMergeDialogOpen={isMergeDialogOpen}
    isDatabaseDialogOpen={isDatabaseDialogOpen}
    isThemeDark={isThemeDark}
    hostAddress={mockPresence.hostAddress}
      currentUser={currentUser}
    currentPageMembers={mockPresence.currentPageMembers}
    differentPageMembers={mockPresence.differentPageMembers}
    handleChooseDatabase={() => null}
    handlePressAddDatabase={() => null}
    handlePressRemoveDatabase={() => null}
    handlePressImportDatabase={() => null}
    activeDatabase={mockDatabases[0]}
    inactiveDatabases={mockDatabases.slice(1, 4)}
    isSynced={true}
      handlePressLeftSidebarToggle={() => setIsLeftSidebarOpen(!isLeftSidebarOpen)}
    handlePressCommandBar={() => setIsCommandBarOpen(!isCommandBarOpen)}
    handlePressDailyNotes={() => setRoute('/daily-notes')}
    handlePressAllPages={() => setRoute('/all-pages')}
    handlePressGraph={() => setRoute('/graph')}
    handlePressThemeToggle={() => setIsThemeDark(!isThemeDark)}
    handlePressMerge={() => setIsMergeDialogOpen(!isMergeDialogOpen)}
    handlePressSettings={() => setRoute('/settings')}
    handlePressDatabase={() => setIsDatabaseDialogOpen(!isDatabaseDialogOpen)}
      handlePressRightSidebarToggle={() => setIsRightSidebarOpen(!isRightSidebarOpen)}
    handlePressMember={(member) => console.log(member)}
      handleUpdateProfile={(person) => setCurrentUser(person)}
    {...args} />
};

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
