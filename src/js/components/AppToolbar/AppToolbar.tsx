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
  Today,
  ToggleOff,
  ToggleOn,
  VerticalSplit
} from '@material-ui/icons';

import {
  HTMLChakraProps,
  ThemingProps,
  Tooltip,
  Flex,
  Button,
  ButtonOptions,
  HStack,
  Divider,
  IconButton,
  ButtonGroup,
  useColorMode
} from '@chakra-ui/react';

import { WindowButtons } from './components/WindowButtons';

interface ToolbarButtonProps extends ButtonOptions, HTMLChakraProps<'button'>, ThemingProps<"Button"> {
  children: React.ReactChild;
};
interface ToolbarIconButtonProps extends ButtonOptions, HTMLChakraProps<'button'>, ThemingProps<"Button"> {
  children: React.ReactChild;
}

const toolbarButtonStyle = {
  background: 'background.floor',
  color: "foreground.secondary",
  WebkitAppRegion: "no-drag",
}

const toolbarIconButtonStyle = {
  background: 'background.floor',
  color: "foreground.secondary",
  WebkitAppRegion: "no-drag",
  sx: {
    "svg": {
      fontSize: "1.5em"
    }
  }
}

const ToolbarButton = React.forwardRef((props: ToolbarButtonProps, ref) => {
  const { children } = props;
  return <Button ref={ref as any} {...toolbarButtonStyle} {...props}> {children}</ Button>
});

const ToolbarIconButton = React.forwardRef((props: ToolbarIconButtonProps, ref) => {
  const { children } = props;
  return <IconButton aria-label="" ref={ref as any} {...toolbarIconButtonStyle
  } {...props}>{children}</IconButton>
});

const AppToolbarWrapper = ({ children, ...props }) => <Flex
  borderBottom="1px solid transparent"
  justifyContent="space-between"
  overflow="hidden"
  py={1}
  px={1}
  h={6}
  zIndex="3"
  userSelect="none"
  transition='border-color 0.5s ease'
  _hover={{
    borderBottomColor: 'separator.divider'
  }}
  sx={{
    WebkitAppRegion: "drag",
    "&.is-right-sidebar-open": {
      borderBottomColor: 'separator.divider'
    },
    ".is-fullscreen &": {
      height: '44px'
    },
    ".os-windows &": {
      background: 'background.floor',
      paddingLeft: '10px',
    },
    ".os-mac &": {
      background: 'background.floor',
      paddingLeft: '22px',
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
    ".os-mac.is-electron &": { paddingLeft: "88px" },
    ".os-mac.is-fullscreen &": {
      paddingLeft: '22px'
    }
  }}
  {...props}
>
  {children}
</Flex>;

export interface AppToolbarProps extends React.HTMLAttributes<HTMLDivElement> {
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

  const { toggleColorMode } = useColorMode()

  return (
    <AppToolbarWrapper className={isRightSidebarOpen ? 'is-right-sidebar-open' : ''} {...rest}>
      <HStack flex="1" justifyContent="space-between">
        <ButtonGroup size="sm">
          {databaseMenu}
          <Tooltip label="Navigation">
            <ToolbarIconButton
              aria-label="Navigation"
              onClick={handlePressLeftSidebarToggle}
              isActive={isLeftSidebarOpen}
            >
              <MenuIcon />
            </ToolbarIconButton>
          </Tooltip>
          {isElectron && (
            <>
              <Divider orientation="vertical" />
              <Tooltip
                label="Go back">
                <ToolbarIconButton aria-label="Go back" onClick={handlePressHistoryBack}><ChevronLeft /></ToolbarIconButton>
              </Tooltip>
              <Tooltip label="Go forward">
                <ToolbarIconButton
                  aria-label="Go forward"
                  onClick={handlePressHistoryForward}>
                  <ChevronRight />
                </ToolbarIconButton>
              </Tooltip>
            </>)
          }
          <Tooltip label="Daily notes">
            <ToolbarIconButton aria-label="Daily notes" isActive={route === '/daily-notes'} onClick={handlePressDailyNotes}>
              <Today />
            </ToolbarIconButton>
          </Tooltip>
          <Tooltip label="All pages">
            <ToolbarIconButton
              aria-label="All pages"
              isActive={route === '/all-pages'} onClick={handlePressAllPages}
            >
              <FileCopy />
            </ToolbarIconButton>
          </Tooltip>
          <Tooltip label="Graph">
            <ToolbarIconButton
              aria-label="Graph"
              isActive={route === '/graph'} onClick={handlePressGraph}>
              <BubbleChart />
            </ToolbarIconButton>
          </Tooltip>
          <ToolbarButton
            aria-label="Search"
            leftIcon={<Search />}
            isActive={isCommandBarOpen}
            onClick={handlePressCommandBar}
            pl="0.5rem"
            border="1px solid"
            borderColor="background.attic"
            _active={{
              bg: 'background.attic',
              borderColor: "background.attic"
            }}
          >
            Find or create a page
          </ToolbarButton>
        </ButtonGroup>
        <ButtonGroup size="sm">
          {presenceDetails}
          <Tooltip label="Merge"><ToolbarIconButton aria-label="Merge" isActive={isMergeDialogOpen} onClick={handlePressMerge}><MergeType /></ToolbarIconButton></Tooltip>
          <Tooltip label="Settings"><ToolbarIconButton aria-label="Settings" isActive={route === '/settings'} onClick={handlePressSettings}><Settings /></ToolbarIconButton></Tooltip>
          <Tooltip label="Toggle theme">
            <ToolbarIconButton onClick={() => {
              toggleColorMode()
              handlePressThemeToggle()
            }}>
              {isThemeDark ? <ToggleOff /> : <ToggleOn />}
            </ToolbarIconButton>
          </Tooltip>
          <Tooltip label="Help"><ToolbarIconButton aria-label="Help" isActive={isHelpOpen} onClick={handlePressHelp}><Help /></ToolbarIconButton></Tooltip>
          <Divider orientation="vertical" />
          <Tooltip label="Show right sidebar"><ToolbarIconButton
            aria-label="Show right sidebar"
            isActive={isRightSidebarOpen}
            onClick={handlePressRightSidebarToggle}
          >
            <VerticalSplit />
          </ToolbarIconButton>
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
