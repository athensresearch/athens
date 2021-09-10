import React from 'react';
import { BADGE, Storybook } from '../../storybook';
import * as mockData from './mockData';

import { useAppState } from '../../useAppState';

import { PresenceDetails } from './PresenceDetails';
import { PresenceDetails as WithUserSettingsDetails } from './Concept/PresenceDetails';

export default {
  title: 'Components/PresenceDetails',
  component: PresenceDetails,
  argTypes: {},
  parameters: {
    // layout: 'centered',
    badges: [BADGE.DEV]
  },
  decorators: [(Story) => {
    return <Storybook.Wrapper><Story /></Storybook.Wrapper>
  }]
};

const Template = (args) => {
  return (<PresenceDetails
    hostAddress={mockData.hostAddress}
    currentPageMembers={mockData.currentPageMembers}
    differentPageMembers={mockData.differentPageMembers}
    {...args} />)
};

export const Basic = Template.bind({});
Basic.args = {}

export const WithUserSettings = () => {
  const {
    currentUser,
    setCurrentUser,
    isOnline,
    setIsOnline,
    route,
    currentPageMembers,
    setCurrentPageMembers,
    differentPageMembers,
    setDifferentPageMembers,
    activeDatabase,
    setActiveDatabase,
    inactiveDatabases,
    setInactiveDatabases,
    isSynced,
    setIsSynced,
    isElectron,
    setIsElectron,
    setRoute,
    hostAddress,
    setHostAddress,
    isThemeDark,
    setIsThemeDark,
    isWinFullscreen,
    setIsWinFullscreen,
    isWinFocused,
    setIsWinFocused,
    isWinMaximized,
    setIsWinMaximized,
    isLeftSidebarOpen,
    setIsLeftSidebarOpen,
    isRightSidebarOpen,
    setIsRightSidebarOpen,
    isSettingsOpen,
    setIsSettingsOpen,
    isCommandBarOpen,
    setIsCommandBarOpen,
    isMergeDialogOpen,
    setIsMergeDialogOpen,
    isDatabaseDialogOpen,
    setIsDatabaseDialogOpen,
    connectionStatus,
  } = useAppState();

  const [isUserSettingsDialogOpen, setIsUserSettingsDialogOpen] = React.useState(false);
  const [currentUserColor, setCurrentUserColor] = React.useState('#ff0000');
  const [currentUserUsername, setCurrentUserUsername] = React.useState('Jeff Tang');

  const handlePressSelf = () => {
    setIsUserSettingsDialogOpen(true);
  }

  return (<WithUserSettingsDetails
    connectionStatus={connectionStatus}
    hostAddress={mockData.hostAddress}
    currentUser={{ username: currentUserUsername, color: currentUserColor, personId: '123' }}
    handlePressSelf={handlePressSelf}
    currentPageMembers={mockData.currentPageMembers}
    differentPageMembers={mockData.differentPageMembers}
    isUserSettingsDialogOpen={isUserSettingsDialogOpen}
    setIsUserSettingsDialogOpen={setIsUserSettingsDialogOpen}
    currentUserColor={currentUserColor}
    setCurrentUserColor={setCurrentUserColor}
    currentUserUsername={currentUserUsername}
    setCurrentUserUsername={setCurrentUserUsername}
    placement="bottom-start"
  />)
};
WithUserSettings.stories = {
  parameters: {
    badges: [BADGE.DEV]
  }
}