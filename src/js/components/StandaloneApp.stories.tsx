import React from 'react';
import styled from 'styled-components';
import { classnames } from '../utils/classnames';

import { LeftSidebar } from './LeftSidebar';
import { RightSidebar } from './RightSidebar';
import { AppToolbar } from './AppToolbar';
import * as mockAppToolbarData from './PresenceDetails/mockData';
import { mockDatabases } from './DatabaseMenu/mockData';
import { CommandBar } from './CommandBar';
import { AppLayout, MainContent } from './App';
import { NodePage } from './Page/Page.stories';

export default {
  title: 'App/Standalone',
  component: Window,
  argTypes: {},
  parameters: {
    layout: 'fullscreen'
  }
};

const Desktop = styled.div`
  background: rebeccapurple;
  display: flex;
`;

const WindowWrapper = styled.div`
  --margin: 2rem;
  margin: var(--margin);
  border-radius: 5px;
  height: calc(100vh - (var(--margin) * 2));
  width: calc(100vw - (var(--margin) * 2));
  box-shadow: 0 10px 12px rgb(0 0 0 / 0.1);
  overflow: hidden;
  position: relative;
  background: var(--background-color);

  > * {
    z-index: 1;
  }

  &.os-windows {
    border-radius: 4px;
  }

  &.os-mac {
    border-radius: 12px;

    &.is-electron {
      &:before {
        content: '';
        width: 12px;
        height: 12px;
        position: absolute;
        border-radius: 100px;
        left: 20px;
        top: 19px;
        background: #888;
        z-index: 999999;
        box-shadow: 20px 0 0 0 #888, 40px 0 0 0 #888;
      }

      &.is-theme-dark {
        &:after {
          content: '';
          position: absolute;
          inset: 0;
          border-radius: inherit;
          pointer-events: none;
          z-index: 2;
          box-shadow: inset 0 0 1px #fff, 0 0 1px #000;
        }
      }
    }
  }

  &.is-win-maximized,
  &.is-win-fullscreen {
    border-radius: 0;
    height: 100vh;
    width: 100vw;
    margin: 0;
  }

  &.is-storybook-docs {
    height: 700px;
  }
`;

const Template = (args, context) => {
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
    <Desktop>
      <WindowWrapper {...args}
        className={classnames(
          'os-' + args.os,
          isWinMaximized && 'is-win-maximized',
          isWinFocused && 'is-win-focused',
          isWinFullscreen && 'is-win-fullscreen',
        )}
      >
        <AppLayout>
          <AppToolbar
            os={args.os}
            isElectron={args.isElectron}
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
            handlePressHistoryBack={() => null}
            handlePressHistoryForward={() => null}
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
      </WindowWrapper>
    </Desktop>)
};

export const MacOs = Template.bind({});
MacOs.args = {
  os: 'mac',
  isElectron: true
};

export const Windows = Template.bind({});
Windows.args = {
  os: 'windows',
  isElectron: true
};

export const Linux = Template.bind({});
Linux.args = {
  os: 'linux',
  isElectron: true
};
