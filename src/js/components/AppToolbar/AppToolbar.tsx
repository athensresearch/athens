import React from 'react';

import {
  RightSidebarIcon,
  MenuIcon,
  HelpIcon,
  ChatFilledIcon,
  ChevronLeftIcon,
  ChevronRightIcon,
  SettingsIcon,
  ContrastIcon,
  EllipsisHorizontalCircleIcon,
  ChatIcon,
} from '@/Icons/Icons';

import {
  Portal,
  Menu,
  MenuButton,
  MenuItem,
  MenuList,
  Tooltip,
  Box,
  Flex,
  Spacer,
  IconButton,
  ButtonGroup,
  useColorMode,
  useMediaQuery,
  ButtonGroupProps,
  useToast
} from '@chakra-ui/react';

import { AnimatePresence, motion } from 'framer-motion';
import { LayoutContext, layoutAnimationProps, layoutAnimationTransition } from "@/Layout/useLayoutState";
import { FakeTrafficLights } from './components/FakeTrafficLights';
import { WindowButtons } from './components/WindowButtons';
import { LocationIndicator } from './components/LocationIndicator';

const PAGE_TITLE_SHOW_HEIGHT = 24;

interface ToolbarButtonGroupProps extends ButtonGroupProps {
  key: string
}

const ToolbarButtonGroup = (props: ToolbarButtonGroupProps) => <ButtonGroup
  as={motion.div} variant="ghost" colorScheme="subtle" size="sm" {...props} />

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
  currentPageTitle?: string;
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

const secondaryToolbarItems = (items) => {
  return <ToolbarButtonGroup key="secondary-items">
    {items.filter(x => !!x).map((item) => <Tooltip closeOnMouseDown label={item.label} key={item.label}>
      <IconButton key={item.label} aria-label={item.label} isActive={item.isActive} onClick={item.onClick} icon={item.icon} />
    </Tooltip>)}
  </ToolbarButtonGroup>
}

const secondaryToolbarOverflowMenu = (items) => {
  return <Menu>
    {({ isOpen }) => <>
      <IconButton as={MenuButton} aria-label="Menu" isActive={isOpen} icon={<EllipsisHorizontalCircleIcon />} />
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
    isThemeDark,
    isLeftSidebarOpen,
    isShowComments,
    onClickComments: handleClickComments,
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
    notificationPopover,
    currentPageTitle,
    presenceDetails,
  } = props;
  const { colorMode, toggleColorMode } = useColorMode();
  const [canShowFullSecondaryMenu] = useMediaQuery('(min-width: 900px)');
  const [isScrolledPastTitle, setIsScrolledPastTitle] = React.useState(null);

  const toast = useToast();

  // add event listener to detect when the user scrolls past the title
  React.useLayoutEffect(() => {
    const scrollContainer = document.getElementById("main-layout") as HTMLElement;

    const handleScroll = () => {
      if (scrollContainer.scrollTop > PAGE_TITLE_SHOW_HEIGHT) {
        setIsScrolledPastTitle(true);
      } else {
        setIsScrolledPastTitle(false);
      }
    }
    handleScroll();
    scrollContainer.addEventListener('scroll', handleScroll);
    return () => scrollContainer.removeEventListener('scroll', handleScroll);
  }, []);

  // If the database color mode doesn't match
  // the chakra color mode, update the chakra color mode
  React.useEffect(() => {
    if (isThemeDark && colorMode !== 'dark') {
      toggleColorMode()
    } else if (!isThemeDark && colorMode !== 'light') {
      toggleColorMode()
    }
  }, [isThemeDark, toggleColorMode]);

  const {
    toolbarRef,
    toolbarHeight,
    mainSidebarWidth
  } = React.useContext(LayoutContext);

  const secondaryTools = [
    handleClickComments && {
      label: isShowComments ? "Hide comments" : "Show comments",
      onClick: () => {
        if (isShowComments) {
          handleClickComments();
          toast({
            title: "Comments hidden",
            status: "info",
            duration: 5000,
            position: "top-right"
          });

        } else {
          handleClickComments();
          toast({
            title: "Comments shown",
            status: "info",
            duration: 5000,
            position: "top-right"
          });

        }
      },
      icon: isShowComments ? <ChatFilledIcon /> : <ChatIcon />
    },
    {
      label: "Help",
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


  const leftSidebarControls = (
    <>
      <Flex
        as={motion.div}
        key="leftSidebar tools"
        {...layoutAnimationProps(isLeftSidebarOpen ? mainSidebarWidth + "px" : "auto")}
        justifyContent="space-between"
        flexShrink={0}
      >

        {/* Left side */}
        <ToolbarButtonGroup
          key="left-sidebar-left-controls"
          pr={3}
          pl={4}
          alignItems="center"
          justifyContent="flex-start"
        >

          {isElectron && os === "mac" && (
            <FakeTrafficLights opacity={isWinFocused ? 1 : 0} />
          )}

          <IconButton
            aria-label="Show navigation"
            onClick={handlePressLeftSidebarToggle}
          >
            <MenuIcon />
          </IconButton>
          {databaseMenu}
        </ToolbarButtonGroup>
        {/* Right side */}
        {isElectron && (
          <ToolbarButtonGroup
            isAttached
            key="left-sidebar-right-controls"
            alignItems="center"
            justifyContent="flex-end"
            pr={3}>
            <Tooltip label="Go back">
              <IconButton
                aria-label="Go back"
                onClick={handlePressHistoryBack}
                icon={<ChevronLeftIcon />}
              />
            </Tooltip>
            <Tooltip label="Go forward">
              <IconButton
                aria-label="Go forward"
                onClick={handlePressHistoryForward}
                icon={
                  <ChevronRightIcon />}
              />
            </Tooltip>
          </ToolbarButtonGroup>)
        }
      </Flex>
    </>
  );

  const rightToolbarControls = (
    <ToolbarButtonGroup
      alignItems="center"
      justifySelf="flex-end"
      key="extras"
      pr={3}
      flex={`0 0 auto`}
      display="flex"
      justifyContent="flex-end"
    >
      {presenceDetails}
      {notificationPopover}
      {canShowFullSecondaryMenu
        ? secondaryToolbarItems(secondaryTools)
        : secondaryToolbarOverflowMenu(secondaryTools)}
    </ToolbarButtonGroup>
  );

  const currentLocationTools = (
    <ToolbarButtonGroup
      key="location tools"
      alignItems="center"
      px={2}
      flex="0 0 auto"
      display="flex"
      justifyContent="flex-start"
    >
      {currentPageTitle && (
        <LocationIndicator
          isVisible={isScrolledPastTitle}
          type="node"
          uid="123"
          title={currentPageTitle}
        />
      )}
    </ToolbarButtonGroup>
  )

  const contentControls = (
    <ToolbarButtonGroup
      alignItems="center"
      key="content tools"
      px={1}
      flex={`1 1 100%`}
      display="flex"
      justifyContent="center"
    />
  )

  const variants = {
    visible: {
      opacity: 1,
      transition: layoutAnimationTransition
    },
    isLeftSidebarOpen: {
      left: mainSidebarWidth,
      transition: layoutAnimationTransition
    },
  }

  return (
    <Flex
      flex={1}
      width="100vw"
      ref={toolbarRef}
      height={toolbarHeight}
      alignItems="center"
      position="fixed"
      inset={0}
      zIndex={2}
      bottom="auto"
      className="toolbar"
      sx={{
        WebkitAppRegion: "drag",
        "button, a": {
          WebkitAppRegion: "no-drag"
        }
      }}
    >
      <AnimatePresence initial={false}>
        {leftSidebarControls}
        {currentLocationTools}
        {contentControls || <Spacer />}
        {rightToolbarControls}

        {(isScrolledPastTitle && (
          <Box
            as={motion.div}
            key="header-backdrop"
            className="header-content-backdrop"
            backdropFilter={`blur(10px)`}
            _after={{
              content: "''",
              position: "absolute",
              inset: 0,
              bg: "background.floor",
              opacity: 0.7,
            }}
            position="absolute"
            zIndex={-1}
            borderBottom="1px solid"
            borderColor="separator.divider"
            height={toolbarHeight}
            right={0}
            initial={{ opacity: 0, left: isLeftSidebarOpen ? mainSidebarWidth : 0 }}
            variants={variants}
            animate={[
              isLeftSidebarOpen && "isLeftSidebarOpen",
              isScrolledPastTitle && "visible"
            ].filter(Boolean)}
            exit={{ opacity: 0 }}
          />
        ))}
      </AnimatePresence>

      {isElectron && (os === 'windows' || os === 'linux') && (
        <WindowButtons
          isWinMaximized={isWinMaximized}
          isWinFullscreen={isWinFullscreen}
          isWinFocused={isWinFocused}
          handlePressMinimize={handlePressMinimize}
          handlePressMaximizeRestore={handlePressMaximizeRestore}
          handlePressClose={handlePressClose}
        />)}
    </Flex>);
};
