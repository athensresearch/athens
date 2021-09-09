import React from 'react';

const mockData = {
  currentUser: {
    personId: '1',
    username: 'John Doe',
    color: '#0071fe'
  }
}

export const useAppState = () => {

  // Session
  const [currentUser, setCurrentUser] = React.useState<Person | null>(mockData.currentUser);

  // Preferences
  const [isThemeDark, setIsThemeDark] = React.useState<boolean>(false);

  // App properties
  const [route, setRoute] = React.useState<string>('');
  const [isOnline, setIsOnline] = React.useState<boolean>(false);

  // Window properties
  const [isWinFullscreen, setIsWinFullscreen] = React.useState<boolean>(false);
  const [isWinFocused, setIsWinFocused] = React.useState<boolean>(true);
  const [isWinMaximized, setIsWinMaximized] = React.useState<boolean>(false);
  const [isElectron, setIsElectron] = React.useState<boolean>(true);

  // Layout components
  const [isLeftSidebarOpen, setIsLeftSidebarOpen] = React.useState<boolean>(false);
  const [isRightSidebarOpen, setIsRightSidebarOpen] = React.useState<boolean>(false);
  const [isCommandBarOpen, setIsCommandBarOpen] = React.useState<boolean>(false);

  // Dialogs and Workflows
  const [isMergeDialogOpen, setIsMergeDialogOpen] = React.useState<boolean>(false);
  const [isDatabaseDialogOpen, setIsDatabaseDialogOpen] = React.useState<boolean>(false);

  return {
    currentUser,
    setCurrentUser,
    isOnline,
    setIsOnline,
    route,
    isElectron,
    setIsElectron,
    setRoute,
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
    isCommandBarOpen,
    setIsCommandBarOpen,
    isMergeDialogOpen,
    setIsMergeDialogOpen,
    isDatabaseDialogOpen,
    setIsDatabaseDialogOpen,
  }
}