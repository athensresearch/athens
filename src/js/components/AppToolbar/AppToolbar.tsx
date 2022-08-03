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
  DailyNotesIcon,
  GraphIcon,
  EllipsisHorizontalCircleIcon,
  CheckmarkIcon,
  ViewIcon,
  ViewOffIcon,
  ChatFilledIcon

} from '@/Icons/Icons';

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
  variant: "ghost",
  colorScheme: "subtle",
}

const toolbarIconButtonStyle = {
  variant: "ghost",
  colorScheme: "subtle",
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
  return <IconButton ref={ref as any} {...toolbarIconButtonStyle
  } {...props}>{children}</IconButton>
});

const AppToolbarWrapper = ({ children, ...props }) => <Flex
  className="app-toolbar"
  gridArea="app-header"
  borderBottom="1px solid transparent"
  justifyContent="space-between"
  // overflow="hidden"
  py={1}
  px={1}
  h={6}
  zIndex="3"
  userSelect="none"
  transition='0.5s ease-in-out'
  transitionProperty='border'
  height="var(--app-header-height, 44px)"
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
    "button, a": {
      WebkitAppRegion: "no-drag"
    },
    ".is-fullscreen &": {
      height: '44px'
    },
    ".os-windows &": {
      pl: '10px',
      py: '0',
      pr: '0',

      "&:hover": {
        background: 'background.floor',
      },
    },
    ".os-linux &": {
      height: '44px',
      pl: '10px',
      py: '0',
      pr: '0',

      "&:hover": {
        background: 'background.floor',
      },
    },
    ".os-mac &": {
      paddingLeft: '22px',
      paddingRight: '22px',
      position: 'absolute',
      top: 0,
      left: 0,
      right: 0,

      "&:hover": {
        background: 'background.floor',
      },
    },
    ".os-mac.is-electron &": {
      paddingLeft: "88px",
      borderTopLeftRadius: '12px',
      borderTopRightRadius: '12px'
    },
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
  /**
  * Whether comments should be shown
  */
  isShowComments: boolean;
  // Electron only
  onPressMinimize?(): void;
  onPressClose?(): void;
  onPressMaximizeRestore?(): void;
  onPressFullscreen?(): void;
  onPressHistoryBack(): void;
  onPressHistoryForward(): void;
  onClickComments(): void;
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
  onPressNotification(): void;
  databaseMenu?: React.FC;
  notificationPopover?: React.FC;
  presenceDetails?: React.FC;
}

const SecondaryToolbarItems = (items) => {
  return <ButtonGroup size="sm">
    {items.filter(x => !!x).map((item) => <Tooltip label={item.label} key={item.label}>
      <ToolbarIconButton variant="ghost" colorScheme="subtle" key={item.label} aria-label={item.label} isActive={item.isActive} onClick={item.onClick}>
        {item.icon}
      </ToolbarIconButton>
    </Tooltip>)}
  </ButtonGroup>
}

const SecondaryToolbarOverflowMenu = (items) => {
  return <Menu>
    {({ isOpen }) => <>
      <ToolbarIconButton size="sm" as={MenuButton} isActive={isOpen}><EllipsisHorizontalCircleIcon /></ToolbarIconButton>
      <Portal>
        <MenuList>
          {items.filter(x => !!x).map((item) => (<MenuItem
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
    isShowComments,
    isNotificationsPopoverOpen,
    onClickComments: handleClickComments,
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
    onClickNotifications: handleShowNotifications,
    handlePressNotifications,
    databaseMenu,
    notificationPopover,
    presenceDetails,
    ...rest
  } = props;
  const { colorMode, toggleColorMode } = useColorMode();
  const [canShowFullSecondaryMenu] = useMediaQuery('(min-width: 900px)');

  // If the database color mode doesn't match
  // the chakra color mode, update the chakra color mode
  React.useEffect(() => {
    if (isThemeDark && colorMode !== 'dark') {
      toggleColorMode()
    } else if (!isThemeDark && colorMode !== 'light') {
      toggleColorMode()
    }
  }, [isThemeDark, toggleColorMode]);

  const secondaryTools = [
    handleClickComments && {
      label: isShowComments ? "Hide comments" : "Show comments",
      isActive: isShowComments,
      onClick: handleClickComments,
      icon: <ChatFilledIcon />
    },
    {
      label: "Help",
      isActive: isHelpOpen,
      onClick: handlePressHelp,
      icon: <HelpIcon />
    },
    {
      label: "Toggle theme",
      onClick: handlePressThemeToggle,
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
    <AppToolbarWrapper
      className={[
        "app-toolbar",
        isRightSidebarOpen && 'is-right-sidebar-open',
      ].filter(Boolean).join(' ')}
      {...rest}
    >
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
              isActive={route === 'home'}
              onClick={handlePressDailyNotes}>
              <DailyNotesIcon />
            </ToolbarIconButton>
          </Tooltip>
          <Tooltip label="All pages">
            <ToolbarIconButton
              aria-label="All pages"
              isActive={route === 'pages'}
              onClick={handlePressAllPages}
            >
              <AllPagesIcon />
            </ToolbarIconButton>
          </Tooltip>
          <Tooltip label="Graph">
            <ToolbarIconButton
              aria-label="Graph"
              isActive={route === 'graph'}
              onClick={handlePressGraph}>
              <GraphIcon />
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

        {notificationPopover}

        {canShowFullSecondaryMenu
          ? SecondaryToolbarItems(secondaryTools)
          : SecondaryToolbarOverflowMenu(secondaryTools)}

      </HStack>
      {isElectron && (os === 'windows' || os === 'linux') && (
        <WindowButtons
          isWinMaximized={isWinMaximized}
          isWinFullscreen={isWinFullscreen}
          isWinFocused={isWinFocused}
          handlePressMinimize={handlePressMinimize}
          handlePressMaximizeRestore={handlePressMaximizeRestore}
          handlePressClose={handlePressClose}
        />)}
    </AppToolbarWrapper>);
};
