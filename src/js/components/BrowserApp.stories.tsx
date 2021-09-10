import styled from 'styled-components';

import { Storybook } from '../storybook';
import { classnames } from '../utils/classnames';
import { getOs } from '../utils/getOs';

import { useAppState } from '../useAppState';

import { LeftSidebar } from './LeftSidebar';
import { RightSidebar } from './RightSidebar';
import { AppToolbar } from './AppToolbar';
import { CommandBar } from './CommandBar';
import { AppLayout, MainContent } from './App';
import { NodePage } from './Page/Page.stories';

export default {
  title: 'App/Browser',
  component: Window,
  argTypes: {},
  parameters: {
    layout: 'fullscreen'
  }
};

const BrowserWrapper = styled.div`
  width: 100%;
  border-radius: 5px;
  box-shadow: 0 10px 12px rgb(0 0 0 / 0.1);
  overflow: hidden;
  position: relative;
  background: var(--background-color);
  --browser-toolbar-height: 48px;

  > * {
    z-index: 1;
  }

  &.is-storybook-docs {
    height: 700px;
  }

  ${AppToolbar} {
    height: calc(100vh - var(--browser-toolbar-height));
    margin-top: 52px;
  }

  #app-layout {
    height: calc(100% - var(--browser-toolbar-height));
    margin-top: var(--browser-toolbar-height);
  }
`;

const BrowserToolbarWrapper = styled.div`
  position: absolute;
  top: 0;
  right: 0;
  left: 0;
  height: var(--browser-toolbar-height);
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--background-minus-2);
    pointer-events: none;

  span {
    padding: 0.25rem 1rem;
    color: var(--body-text-color---opacity-med);
    font-weight: bold;
  }

  input {
    border-radius: 100em;
    padding: 0.25rem;
    border: 0;
    background: var(--background-plus-1);
    color: inherit;
    width: max(70%, 70em);
    height: 60%;
    text-align: center;
    margin: auto;
    color: var(--body-text-color---opacity-med);
  }
`;

const BrowserToolbar = () => {
  return (
    <BrowserToolbarWrapper>
      <span>Browser</span>
      <input readOnly={true} tabIndex={-1} defaultValue="athens" />
    </BrowserToolbarWrapper>
  )
}

const Template = (args, context) => {
  const {
    currentUser,
    setCurrentUser,
    isSynced,
    route,
    currentPageMembers,
    differentPageMembers,
    activeDatabase,
    setActiveDatabase,
    inactiveDatabases,
    connectionStatus,
    isElectron,
    setRoute,
    hostAddress,
    isThemeDark,
    setIsThemeDark,
    isWinFullscreen,
    isWinFocused,
    isWinMaximized,
    isLeftSidebarOpen,
    setIsLeftSidebarOpen,
    isRightSidebarOpen,
    setIsRightSidebarOpen,
    setIsSettingsOpen,
    isCommandBarOpen,
    setIsCommandBarOpen,
    isMergeDialogOpen,
    setIsMergeDialogOpen,
    isDatabaseDialogOpen,
  } = useAppState();

  return (
    <Storybook.Desktop>
      <BrowserWrapper {...args}
        className={classnames(
          isWinMaximized && 'is-win-maximized',
          isWinFocused && 'is-win-focused',
          isWinFullscreen && 'is-win-fullscreen',
        )}
      >
        <AppLayout>
          <BrowserToolbar />
          <AppToolbar
            os={args.os}
            route={route}
            isElectron={isElectron}
            isWinFullscreen={isWinFullscreen}
            isWinFocused={isWinFocused}
            isWinMaximized={isWinMaximized}
            isThemeDark={isThemeDark}
            isLeftSidebarOpen={isLeftSidebarOpen}
            isRightSidebarOpen={isRightSidebarOpen}
            isCommandBarOpen={isCommandBarOpen}
            isMergeDialogOpen={isMergeDialogOpen}
            isDatabaseDialogOpen={isDatabaseDialogOpen}
            hostAddress={hostAddress}
            currentUser={currentUser}
            currentPageMembers={currentPageMembers}
            differentPageMembers={differentPageMembers}
            activeDatabase={activeDatabase}
            inactiveDatabases={inactiveDatabases}
            connectionStatus={connectionStatus}
            isSynced={isSynced}
            handleChooseDatabase={(database) => setActiveDatabase(database)}
            handlePressAddDatabase={() => console.log('pressed add database')}
            handlePressRemoveDatabase={() => console.log('pressed remove database')}
            handlePressImportDatabase={() => console.log('pressed import database')}
            handlePressMoveDatabase={() => console.log('pressed move database')}
            handlePressMember={(person) => console.log(person)}
            handlePressCommandBar={() => setIsCommandBarOpen(!isCommandBarOpen)}
            handlePressDailyNotes={() => setRoute('/daily-notes')}
            handlePressAllPages={() => setRoute('/all-pages')}
            handlePressGraph={() => setRoute('/graph')}
            handlePressThemeToggle={() => setIsThemeDark(!isThemeDark)}
            handlePressMerge={() => setIsMergeDialogOpen(true)}
            handlePressSettings={() => setIsSettingsOpen(true)}
            handlePressHistoryBack={() => console.log('pressed go back')}
            handlePressHistoryForward={() => console.log('pressed go forward')}
            handlePressLeftSidebarToggle={() => setIsLeftSidebarOpen(!isLeftSidebarOpen)}
            handlePressRightSidebarToggle={() => setIsRightSidebarOpen(!isRightSidebarOpen)}
            handlePressMinimize={() => console.log('pressed minimize')}
            handlePressMaximizeRestore={() => console.log('pressed maximize/restore')}
            handlePressClose={() => console.log('pressed close')}
            handlePressHostAddress={(hostAddress) => console.log('pressed', hostAddress)}
            handleUpdateProfile={(person) => setCurrentUser(person)}
          />
          <LeftSidebar
            isLeftSidebarOpen={isLeftSidebarOpen}
            handlePressShortcut={() => null}
            shortcuts={[{
              uid: "4b89dde0-3ccf-481a-875b-d11adfda3f7e",
              title: "Passer domesticus",
              order: 1
            }, {
              uid: "bd4a892f-c7e5-45d8-bab8-68a8ed9d224f",
              title: "Spermophilus richardsonii",
              order: 2
            }, {
              uid: "b60fc12e-bf48-415c-a059-a7a4d5ef686e",
              title: "Leprocaulinus vipera",
              order: 3
            }, {
              uid: "c58d62e5-0e1b-4f30-a156-af8467317c1c",
              title: "Rangifer tarandus",
              order: 4
            }, {
              uid: "dd099e5d-1f6d-4be7-8bf0-9fc0310ba489",
              title: "Nycticorax nycticorax",
              order: 5
            }]}
            version="1.0.0"
          />
          <MainContent>
            <NodePage />
          </MainContent>
          <RightSidebar isRightSidebarOpen={isRightSidebarOpen} />
          {/* <Devtool /> */}
          {isCommandBarOpen && (<CommandBar
            autoFocus={true}
            handleCloseCommandBar={() => setIsCommandBarOpen(false)}
          />)
          }
        </AppLayout>
      </BrowserWrapper>
    </Storybook.Desktop>)
};

export const Browser = Template.bind({});
Browser.args = {
  os: () => getOs(window)
};
