import React from 'react';
import {
  BubbleChart,
  ChevronLeft,
  ChevronRight,
  FileCopy,
  Help,
  Menu as MenuIcon,
  MergeType,
  Search,
  Settings,
  Storage,
  Today,
  ToggleOff,
  ToggleOn,
  VerticalSplit
} from '@material-ui/icons';

import {
  Tooltip,
  Flex,
  Button,
  HStack,
  Divider,
  IconButton,
  ButtonGroup
} from '@chakra-ui/react';

import { WindowButtons } from './components/WindowButtons';

const AppToolbarWrapper = ({ children }) => <Flex
  borderBottom="1px solid transparent"
  justifyContent="space-between"
  py={1}
  px={1}
  h={6}
  zIndex="10"
  _hover={{
    transition: 'border-color 0.15s ease',
    borderBottomColor: 'separator.border'
  }}
  sx={{
    "-webkit-app-region": "drag",

    ".is-fullscreen &": {
      height: '44px'
    },
    ".os-windows &": {
      background: 'background.floor',
      paddingLeft: '10px',
    },
    ".os-mac &": {
      background: 'background.floor',
      paddingLeft: '88px',
      paddingRight: '22px',
      height: '52px',
      borderTopLeftRadius: '12px',
      borderTopRightRadius: '12px',
      backdropFilter: 'blur(20px)',
      position: 'absolute',
      top: 0,
      left: 0,
      right: 0,
    },
    ".os-mac.is-fullscreen &": {
      paddingLeft: '22px'
    }
  }}
>
  {children}
</Flex>;

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
  os: OS;
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
  * Whether the help dialog is open
  */
  isHelpOpen: boolean;
  /**
  * Whether the theme is set to dark mode
  */
  isThemeDark: boolean;
  // Electron only
  onPressMinimize?(): void;
  onPressClose?(): void;
  onPressMaximizeRestore?(): void;
  onPressFullscreen?(): void;
  onPressHistoryBack(): void;
  onPressHistoryForward(): void;
  // Main toolbar
  onPressCommandBar(): void;
  onPressDailyNotes(): void;
  onPressAllPages(): void;
  onPressGraph(): void;
  onPressHelp(): void;
  onPressThemeToggle(): void;
  onPressMerge(): void;
  onPressSettings(): void;
  onPressHistoryBack(): void;
  onPressHistoryForward(): void;
  onPressLeftSidebarToggle(): void;
  onPressRightSidebarToggle(): void;
  databaseMenu?: React.FC;
  presenceDetails?: React.FC;
}

export const AppToolbar = (props: AppToolbarProps): React.ReactElement => {
  const {
    os,
    route,
    isElectron,
    isWinFullscreen,
    isWinFocused,
    isWinMaximized,
    isHelpOpen,
    isThemeDark,
    isLeftSidebarOpen,
    isRightSidebarOpen,
    isCommandBarOpen,
    isMergeDialogOpen,
    onPressCommandBar: handlePressCommandBar,
    onPressDailyNotes: handlePressDailyNotes,
    onPressAllPages: handlePressAllPages,
    onPressGraph: handlePressGraph,
    onPressHelp: handlePressHelp,
    onPressThemeToggle: handlePressThemeToggle,
    onPressMerge: handlePressMerge,
    onPressSettings: handlePressSettings,
    onPressHistoryBack: handlePressHistoryBack,
    onPressHistoryForward: handlePressHistoryForward,
    onPressLeftSidebarToggle: handlePressLeftSidebarToggle,
    onPressRightSidebarToggle: handlePressRightSidebarToggle,
    onPressMinimize: handlePressMinimize,
    onPressMaximizeRestore: handlePressMaximizeRestore,
    onPressClose: handlePressClose,
    databaseMenu,
    presenceDetails,
    ...rest
  } = props;

  return (
    <AppToolbarWrapper {...rest}>
      <HStack flex="1" justifyContent="space-between">
        <ButtonGroup size="sm">
          {databaseMenu}
          <Tooltip label="Navigation">
            <IconButton
              aria-label="Navigation"
              onClick={handlePressLeftSidebarToggle}
              isActive={isLeftSidebarOpen}
            >
              <MenuIcon />
            </IconButton>
          </Tooltip>
          {isElectron && (
            <>
              <Divider orientation="vertical" />
              <Tooltip
                label="Go back">
                <IconButton aria-label="Go back" onClick={handlePressHistoryBack}><ChevronLeft /></IconButton>
              </Tooltip>
              <Tooltip label="Go forward">
                <IconButton
                  aria-label="Go forward"
                  onClick={handlePressHistoryForward}><ChevronRight /></IconButton>
              </Tooltip>
            </>)
          }
          <Tooltip label="Daily notes"><IconButton aria-label="Daily notes" isActive={route === '/daily-notes'} onClick={handlePressDailyNotes}><Today /></IconButton></Tooltip>
          <Tooltip label="All pages"><IconButton
            aria-label="All pages"
            isActive={route === '/all-pages'} onClick={handlePressAllPages}><FileCopy /></IconButton></Tooltip>
          <Tooltip label="Graph"><IconButton
            aria-label="Graph"
            isActive={route === '/graph'} onClick={handlePressGraph}><BubbleChart /></IconButton></Tooltip>
          <Button
            aria-label="Search"
            leftIcon={<Search />}
            isActive={isCommandBarOpen}
            onClick={handlePressCommandBar}
          >
            Find or create a page
          </Button>
        </ButtonGroup>
        <ButtonGroup size="sm">
          {presenceDetails}
          <Tooltip label="Merge"><IconButton aria-label="Merge" isActive={isMergeDialogOpen} onClick={handlePressMerge}><MergeType /></IconButton></Tooltip>
          <Tooltip label="Settings"><IconButton aria-label="Settings" isActive={route === '/settings'} onClick={handlePressSettings}><Settings /></IconButton></Tooltip>
          <Tooltip label="Toggle theme"><IconButton aria-label="Toggle theme" onClick={handlePressThemeToggle}>
            {isThemeDark ? <ToggleOff /> : <ToggleOn />}
          </IconButton></Tooltip>
          <Tooltip label="Help"><IconButton aria-label="Help" isActive={isHelpOpen} onClick={handlePressHelp}><Help /></IconButton></Tooltip>
          <Divider orientation="vertical" />
          <Tooltip label="Show right sidebar"><IconButton
            aria-label="Show right sidebar"
            isActive={isRightSidebarOpen}
            onClick={handlePressRightSidebarToggle}
          >
            <VerticalSplit />
          </IconButton>
          </Tooltip>
        </ButtonGroup>
      </HStack>
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
