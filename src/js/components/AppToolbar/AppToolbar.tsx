import React from 'react';
import styled from 'styled-components';
import { BubbleChart, ChevronLeft, ChevronRight, FileCopy, Menu as MenuIcon, MergeType, Search, Settings, Storage, Today, ToggleOff, ToggleOn, VerticalSplit } from '@material-ui/icons';

import { Button } from '../Button';

import { DatabaseMenu, DatabaseMenuProps } from '../DatabaseMenu';
import { WindowButtons } from './components/WindowButtons';
import { PresenceDetails, PresenceDetailsProps } from '../PresenceDetails/Concept/PresenceDetails';
import * as mockPresence from '../PresenceDetails/mockData';


const AppToolbarWrapper = styled.header`
  background: var(--color-background);
  grid-area: app-header;
  justify-content: flex-start;
  background-clip: padding-box;
  background: var(--background-plus-1);
  color: var(--body-text-color---opacity-high);
  border-bottom: 1px solid transparent;
  align-items: center;
  display: grid;
  height: 48px;
  padding-left: 10px;
  grid-template-columns: auto 1fr auto;
  transition: border-color 1s ease;
  z-index: 1070;
  grid-auto-flow: column;
  -webkit-app-region: drag;

  .is-fullscreen & {
    height: 44px;
  }

  svg {
    font-size: 20px;
  }

  &:hover {
    transition: border-color 0.15s ease;
    border-bottom-color: var(--body-text-color---opacity-lower);
  }

  button {
    justify-self: flex-start;
    -webkit-app-region: no-drag;
  }

  .os-windows & {
    background: var(--background-minus-1);
    padding-left: 10px;
  }

  .os-mac & {
    background: var(--background-color---opacity-high);
    color: var(--body-text-color---opacity-med);
    padding-left: 88px;
    padding-right: 22px;
    height: 52px;
    border-top-left-radius: 12px;
    border-top-right-radius: 12px;
    backdrop-filter: blur(20px);
    position: absolute;
    top: 0;
    left: 0;
    right: 0;

    .is-fullscreen & {
      padding-left: 22px;
    }
  }
`;

export interface AppToolbarProps extends React.HTMLAttributes<HTMLDivElement>, DatabaseMenuProps, PresenceDetailsProps {
  /**
  * The application's current route
  */
  route: string;
  /**
  * If the app is in Electron, whether or not it has user focus
  */
  isWinFocused: boolean;
  /**
  * If the app is in Electron, whether or not it is fullscreen
  */
  isWinFullscreen: boolean;
  /**
  * If the app is in Electron, whether or not it is maximized
  */
  isWinMaximized: boolean;
  /**
  * The name of the host OS
  */
  os: string;
  /**
  * Whether the renderer is in Electron or a browser
  */
  isElectron: boolean;
  /**
  * Whether the shortcuts sidebar is open
  */
  isLeftSidebarOpen: boolean;
  /**
  * Whether the reference sidebar is open
  */
  isRightSidebarOpen: boolean;
  /**
  * Whether the search/create command bar is open
  */
  isCommandBarOpen: boolean;
  /**
  * Whether the merge from roam dialog is open
  */
  isMergeDialogOpen: boolean;
  /**
  * Whether the choose database dialog is open
  */
  isDatabaseDialogOpen: boolean;
  /**
  * Whether the theme is set to dark mode
  */
  isThemeDark: boolean;
  // Electron only
  handlePressMinimize?(): void;
  handlePressClose?(): void;
  handlePressMaximizeRestore?(): void;
  handlePressMaximizeRestore?(): void;
  handlePressMaximizeRestore?(): void;
  handlePressFullscreen?(): void;
  // Main toolbar
  handlePressCommandBar(): void;
  handlePressDailyNotes(): void;
  handlePressAllPages(): void;
  handlePressGraph(): void;
  handlePressThemeToggle(): void;
  handlePressMerge(): void;
  handlePressSettings(): void;
  handlePressDatabase(): void;
  handlePressHistoryBack(): void;
  handlePressHistoryForward(): void;
  handlePressLeftSidebar(): void;
  handlePressRightSidebar(): void;
  // Presence Details
  handleUpdateProfile(person): void;
  handlePressHostAddress?(): void;
  handlePressMember?(member): void;
  // DB Menu
  handleChooseDatabase: (db: Database) => void
  handlePressAddDatabase: () => void
  handlePressRemoveDatabase: (db: Database) => void
  handlePressImportDatabase: (db: Database) => void
  handlePressMoveDatabase: (db: Database) => void
}

export const useAppToolbarState = () => {
  const [route, setRoute] = React.useState('');
  const [isWinFullscreen, setIsWinFullscreen] = React.useState(false);
  const [isWinFocused, setIsWinFocused] = React.useState(true);
  const [isWinMaximized, setIsWinMaximized] = React.useState(false);
  const [isLeftSidebarOpen, setIsLeftOpen] = React.useState(false);
  const [isRightSidebarOpen, setIsRightSidebarOpen] = React.useState(false);
  const [isCommandBarOpen, setIsCommandBarOpen] = React.useState(false);
  const [isMergeDialogOpen, setIsMergeDialogOpen] = React.useState(false);
  const [isDatabaseDialogOpen, setIsDatabaseDialogOpen] = React.useState(false);
  const [isThemeDark, setIsThemeDark] = React.useState(false);
  const [currentUser, setCurrentUser] = React.useState(mockPresence.currentUser);

  return {
    route,
    setRoute,
    isWinFullscreen,
    setIsWinFullscreen,
    isWinFocused,
    setIsWinFocused,
    isWinMaximized,
    setIsWinMaximized,
    isLeftSidebarOpen,
    setIsLeftOpen,
    isRightSidebarOpen,
    setIsRightSidebarOpen,
    isCommandBarOpen,
    setIsCommandBarOpen,
    isMergeDialogOpen,
    setIsMergeDialogOpen,
    isDatabaseDialogOpen,
    setIsDatabaseDialogOpen,
    isThemeDark,
    setIsThemeDark,
    currentUser,
    setCurrentUser,
  }
}

export const AppToolbar = ({
  route,
  os,
  isElectron,
  isWinFullscreen,
  isWinFocused,
  isWinMaximized,
  isThemeDark,
  isLeftSidebarOpen,
  isRightSidebarOpen,
  isCommandBarOpen,
  isMergeDialogOpen,
  isDatabaseDialogOpen,
  hostAddress,
  currentUser,
  currentPageMembers,
  differentPageMembers,
  activeDatabase,
  inactiveDatabases,
  synced,
  handleChooseDatabase,
  handlePressAddDatabase,
  handlePressRemoveDatabase,
  handlePressImportDatabase,
  handlePressMoveDatabase,
  handlePressMember,
  handlePressCommandBar,
  handlePressDailyNotes,
  handlePressAllPages,
  handlePressGraph,
  handlePressThemeToggle,
  handlePressMerge,
  handlePressSettings,
  handlePressDatabase,
  handlePressHistoryBack,
  handlePressHistoryForward,
  handlePressLeftSidebar,
  handlePressRightSidebar,
  handlePressMinimize,
  handlePressMaximizeRestore,
  handlePressClose,
  handlePressHostAddress,
  handleUpdateProfile,
}: AppToolbarProps): React.ReactElement => {

  return (<AppToolbarWrapper>
    <AppToolbar.MainControls>
      <DatabaseMenu
        activeDatabase={activeDatabase}
        inactiveDatabases={inactiveDatabases}
        synced={synced}
        handleChooseDatabase={handleChooseDatabase}
        handlePressAddDatabase={handlePressAddDatabase}
        handlePressRemoveDatabase={handlePressRemoveDatabase}
        handlePressImportDatabase={handlePressImportDatabase}
        handlePressMoveDatabase={handlePressMoveDatabase}
      />
      <Button
        onClick={handlePressLeftSidebar}
        isPressed={isLeftSidebarOpen}
      >
        <MenuIcon />
      </Button>
      {isElectron && (
        <>
          <AppToolbar.Separator />
          <Button onClick={handlePressHistoryBack}><ChevronLeft /></Button>
          <Button onClick={handlePressHistoryForward}><ChevronRight /></Button>
        </>)
      }
      <Button isPressed={route === '/daily-notes'} onClick={handlePressDailyNotes}><Today /></Button>
      <Button isPressed={route === '/all-pages'} onClick={handlePressAllPages}><FileCopy /></Button>
      <Button isPressed={route === '/graph'} onClick={handlePressGraph}><BubbleChart /></Button>
      <Button isPressed={isCommandBarOpen} onClick={handlePressCommandBar}><Search /> <span>Find or create a page</span></Button>
    </AppToolbar.MainControls>
    <AppToolbar.SecondaryControls>
      <PresenceDetails
        currentUser={currentUser}
        currentPageMembers={currentPageMembers}
        differentPageMembers={differentPageMembers}
        hostAddress={hostAddress}
        placement="bottom-end"
        handlePressHostAddress={handlePressHostAddress}
        handlePressMember={handlePressMember}
        handleUpdateProfile={handleUpdateProfile}
      />
      <Button isPressed={isMergeDialogOpen} onClick={handlePressMerge}><MergeType /></Button>
      <Button isPressed={route === '/settings'} onClick={handlePressSettings}><Settings /></Button>
      <Button isPressed={isDatabaseDialogOpen} onClick={handlePressDatabase}><Storage /></Button>
      <Button
        onClick={handlePressThemeToggle}>
        {isThemeDark ? <ToggleOff /> : <ToggleOn />}
      </Button>
      <AppToolbar.Separator />
      <Button
        isPressed={isRightSidebarOpen}
        onClick={handlePressRightSidebar}
      >
        <VerticalSplit />
      </Button>
    </AppToolbar.SecondaryControls>
    {isElectron && (os === 'windows' || os === 'linux') && (
      <WindowButtons
        os={os}
        isWinMaximized={isWinMaximized}
        isWinFullscreen={isWinFullscreen}
        isWinFocused={isWinFocused}
        handlePressMinimize={handlePressMinimize}
        handlePressMaximizeRestore={handlePressMaximizeRestore}
        handlePressClose={handlePressClose}
      />)}
  </AppToolbarWrapper>);
};

AppToolbar.Separator = styled.hr`
  border: 0;
  margin-inline: 0.125rem;
  margin-block: 0;
  block-size: auto;
`;

AppToolbar.MainControls = styled.div`
  display: grid;
  grid-auto-flow: column;
  grid-gap: 0.25rem;
  align-items: center;
`;

AppToolbar.SecondaryControls = styled(AppToolbar.MainControls)`
  justify-self: flex-end;
  margin-left: auto;

  button {
    color: inherit;
    background: inherit;
  }
`;
