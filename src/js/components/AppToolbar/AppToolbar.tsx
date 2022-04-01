import React from 'react';

import {
  RightSidebarIcon,
  SearchIcon,
  MenuIcon,
  HelpIcon,
  ChevronLeftIcon,
  ChevronRightIcon,
  AllPagesIcon,
  SettingsIcon,
  ContrastIcon,
  DailyNotesIcon
} from '@/Icons/Icons';

import {
  BubbleChart,
  MoreHoriz,
} from '@material-ui/icons';

import {
  HTMLChakraProps,
  Portal,
  ThemingProps,
  Menu,
  MenuButton,
  MenuItem,
  MenuList,
  Tooltip,
  Flex,
  Button,
  ButtonOptions,
  HStack,
  IconButton,
  ButtonGroup,
  useColorMode,
  useMediaQuery
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
  sx: { WebkitAppRegion: "no-drag" }
}

const toolbarIconButtonStyle = {
  background: 'background.floor',
  color: "foreground.secondary",
  sx: {
    WebkitAppRegion: "no-drag",
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
  transition='common 0.5s ease-in-out'
  _hover={{
    borderBottomColor: 'separator.divider',
    bg: "background.floor",
  }}
  sx={{
    WebkitAppRegion: "drag",
    "&.is-right-sidebar-open": {
      borderBottomColor: 'separator.divider',
      bg: "background.floor",
    },
    ".is-fullscreen &": {
      height: '44px'
    },
    ".os-windows &": {
      "&:hover": {
        background: 'background.floor',
      },
      paddingLeft: '10px',
    },
    ".os-mac &": {
      paddingLeft: '22px',
      paddingRight: '22px',
      height: '52px',
      borderTopLeftRadius: '12px',
      borderTopRightRadius: '12px',
      position: 'absolute',
      top: 0,
      left: 0,
      right: 0,
      "&:hover": {
        background: 'background.floor',
      },
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
  onPressSettings(): void;
  onPressHistoryBack(): void;
  onPressHistoryForward(): void;
  onPressLeftSidebarToggle(): void;
  onPressRightSidebarToggle(): void;
  databaseMenu?: React.FC;
  presenceDetails?: React.FC;
}

const SecondaryToolbarItems = (items) => {
  return <ButtonGroup size="sm">
    {items.map((item) => <Tooltip label={item.label} key={item.label}>
      <ToolbarIconButton key={item.label} aria-label={item.label} isActive={item.isActive} onClick={item.onClick}>
        {item.icon}
      </ToolbarIconButton>
    </Tooltip>)}
  </ButtonGroup>
}

const SecondaryToolbarOverflowMenu = (items) => {
  return <Menu>
    {({ isOpen }) => <>
      <ToolbarIconButton size="sm" as={MenuButton} isActive={isOpen}><MoreHoriz /></ToolbarIconButton>
      <Portal>
        <MenuList>
          {items.map((item) => (<MenuItem
            key={item.label}
            onClick={item.onClick}
            icon={item.icon}
          >
            {item.label}
          </MenuItem>
          ))}
        </MenuList>
      </Portal>
    </>
    }
  </Menu>
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
    onPressCommandBar: handlePressCommandBar,
    onPressDailyNotes: handlePressDailyNotes,
    onPressAllPages: handlePressAllPages,
    onPressGraph: handlePressGraph,
    onPressHelp: handlePressHelp,
    onPressThemeToggle: handlePressThemeToggle,
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
  const [ canShowFullSecondaryMenu ] = useMediaQuery('(min-width: 900px)');

  const secondaryTools = [
    {
      label: "Help",
      isActive: isHelpOpen,
      onClick: handlePressHelp,
      icon: <HelpIcon />
    },
    {
      label: "Toggle theme",
      onClick: () => {
        toggleColorMode()
        handlePressThemeToggle()
      },
      icon: <ContrastIcon />
    },
    {
      label: "Settings",
      isActive: route === '/settings',
      onClick: handlePressSettings,
      icon: <SettingsIcon />
    },
    {
      label: 'Show right sidebar',
      onClick: handlePressRightSidebarToggle,
      icon: <RightSidebarIcon />
    }
  ];

  return (
    <AppToolbarWrapper className={isRightSidebarOpen ? 'is-right-sidebar-open' : ''} {...rest}>
      <HStack flex="1">
        <ButtonGroup size="sm" mr="auto">
          {databaseMenu}
          <Tooltip label="Navigation">
            <ToolbarIconButton
              aria-label="Navigation"
              onClick={handlePressLeftSidebarToggle}
            >
              <MenuIcon />
            </ToolbarIconButton>
          </Tooltip>
          {isElectron && (
            <ButtonGroup isAttached size="sm">
              <Tooltip
                label="Go back">
                <ToolbarIconButton
                  aria-label="Go back"
                  onClick={handlePressHistoryBack}
                >
                  <ChevronLeftIcon />
                </ToolbarIconButton>
              </Tooltip>
              <Tooltip label="Go forward">
                <ToolbarIconButton
                  aria-label="Go forward"
                  onClick={handlePressHistoryForward}>
                  <ChevronRightIcon />
                </ToolbarIconButton>
              </Tooltip>
            </ButtonGroup>)
          }
          <Tooltip label="Daily notes">
            <ToolbarIconButton
              aria-label="Daily notes"
              isDisabled={route === '/daily-notes'}
              isActive={route === '/daily-notes'}
              onClick={handlePressDailyNotes}>
              <DailyNotesIcon />
            </ToolbarIconButton>
          </Tooltip>
          <Tooltip label="All pages">
            <ToolbarIconButton
              aria-label="All pages"
              isDisabled={route === '/all-pages'}
              isActive={route === '/all-pages'} onClick={handlePressAllPages}
            >
              <AllPagesIcon />
            </ToolbarIconButton>
          </Tooltip>
          <Tooltip label="Graph">
            <ToolbarIconButton
              aria-label="Graph"
              isDisabled={route === '/graph'}
              isActive={route === '/graph'} onClick={handlePressGraph}>
              <BubbleChart />
            </ToolbarIconButton>
          </Tooltip>
          <ToolbarButton
            aria-label="Search"
            variant="outline"
            leftIcon={<SearchIcon />}
            isActive={isCommandBarOpen}
            onClick={handlePressCommandBar}
            pl="0.5rem"
          >
            Find or create a page
          </ToolbarButton>
        </ButtonGroup>

        {presenceDetails}

        {canShowFullSecondaryMenu
          ? SecondaryToolbarItems(secondaryTools)
          : SecondaryToolbarOverflowMenu(secondaryTools)}0

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
