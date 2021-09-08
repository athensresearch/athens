import React from 'react';
import styled from 'styled-components';

import { Storybook } from '../storybook';
import { classnames } from '../utils/classnames';
import { getOs } from '../utils/getOs';

import { LeftSidebar } from './LeftSidebar';
import { RightSidebar } from './RightSidebar';
import { AppToolbar } from './AppToolbar';
import * as mockAppToolbarData from './PresenceDetails/mockData';
import { mockDatabases } from './DatabaseMenu/mockData';
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
  const [os, setOs] = React.useState(args.os);
  const [route, setRoute] = React.useState(args.route);
  const [isWinFullscreen, setIsWinFullscreen] = React.useState(args.isWinFullscreen);
  const [isWinFocused, setIsWinFocused] = React.useState(args.isWinFocused);
  const [isWinMaximized, setIsWinMaximized] = React.useState(args.isWinMaximized);
  const [isLeftSidebarOpen, setIsLeftOpen] = React.useState(args.isLeftSidebarOpen || true);
  const [isRightSidebarOpen, setIsRightSidebarOpen] = React.useState(args.isRightSidebarOpen || false);
  const [isCommandBarOpen, setIsCommandBarOpen] = React.useState(args.isCommandBarOpen || false);
  const [isMergeDialogOpen, setIsMergeDialogOpen] = React.useState(args.isMergeDialogOpen);
  const [isDatabaseDialogOpen, setIsDatabaseDialogOpen] = React.useState(args.isDatabaseDialogOpen);
  const [isThemeDark, setIsThemeDark] = React.useState(args.isThemeDark);

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
            os={os}
            isElectron={false}
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
            hostAddress={mockAppToolbarData.hostAddress}
            currentPageMembers={mockAppToolbarData.currentPageMembers}
            differentPageMembers={mockAppToolbarData.differentPageMembers}
            activeDatabase={mockDatabases[0]}
            inactiveDatabases={mockDatabases.slice(1, 4)}
            synced={true}
            handleChooseDatabase={() => null}
            handlePressAddDatabase={() => null}
            handlePressRemoveDatabase={() => null}
            handlePressImportDatabase={() => null}
            handlePressMoveDatabase={() => null}
            handlePressHistoryBack={() => null}
            handlePressHistoryForward={() => null}
            handlePressCommandBar={() => setIsCommandBarOpen(!isCommandBarOpen)}
            handlePressDailyNotes={() => setRoute('/daily-notes')}
            handlePressAllPages={() => setRoute('/all-pages')}
            handlePressGraph={() => setRoute('/graph')}
            handlePressThemeToggle={() => setIsThemeDark(!isThemeDark)}
            handlePressMerge={() => setIsMergeDialogOpen(!isMergeDialogOpen)}
            handlePressSettings={() => setRoute('/settings')}
            handlePressDatabase={() => setIsDatabaseDialogOpen(!isDatabaseDialogOpen)}
            handlePressLeftSidebar={() => setIsLeftOpen(!isLeftSidebarOpen)}
            handlePressRightSidebar={() => setIsRightSidebarOpen(!isRightSidebarOpen)}
            handlePressFullscreen={() => setIsWinFullscreen(true)}
            handlePressMaximizeRestore={() => setIsWinMaximized(!isWinMaximized)}
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
