import React from 'react';
import styled from 'styled-components';
import { classnames } from '../util/classnames';
import { getOs } from '../util/getOs';

import { LeftSidebar } from './LeftSidebar';
import { RightSidebar } from './RightSidebar';
import { AppToolbar } from './AppToolbar';
import { CommandBar } from './CommandBar';
import { AppLayout } from './AppLayout';
import { WelcomePage } from './Page/Page.stories';
import { Block } from './Block';

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


export default {
  title: 'App/Electron',
  component: Window,
  argTypes: {},
};

const Template = (args, context) => {
  const [os, setOs] = React.useState(args.os);
  const [route, setRoute] = React.useState(args.route);
  const [isElectron, setIsElectron] = React.useState(args.isElectron);
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
          "os-" + os,
          isElectron ? 'is-electron' : 'is-web',
          isWinMaximized && 'is-win-maximized',
          isWinFocused && 'is-win-focused',
          isWinFullscreen && 'is-win-fullscreen',
          isThemeDark ? 'is-theme-dark' : 'is-theme-light',
          context.viewMode === 'docs' ? 'is-storybook-docs' : 'is-in-canvas',
        )}
      >
        <AppLayout>
          <AppToolbar
            os={os}
            isElectron={isElectron}
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
            shortcuts={[{ uid: '1', title: 'James and the giant', order: 0 }]}
            version="1.0.0"
          />
          <WelcomePage />
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

export const Primary = Template.bind({});
Primary.args = {
  os: () => getOs(window),
  isElectron: true,
};
Primary.parameters = {
  layout: 'fullscreen',
}

export const MacOs = Template.bind({});
MacOs.args = {
  os: 'mac',
  isElectron: true
};
MacOs.parameters = {
  layout: 'fullscreen',
}

export const Windows = Template.bind({});
Windows.args = {
  os: 'windows',
  isElectron: true
};
Windows.parameters = {
  layout: 'fullscreen',
}

export const Linux = Template.bind({});
Linux.args = {
  os: 'linux',
  isElectron: true
};
Linux.parameters = {
  layout: 'fullscreen',
}
