import React from 'react';
import styled from 'styled-components';
import { classnames } from '../util/classnames';
import { getOs } from '../util/getOs';

import { AppToolbar } from './AppToolbar';
import { Block } from './Block';

const Window = styled.div`
  --margin: 2rem;
  background: #0dd;
  margin: var(--margin);
  overflow: hidden;
  border-radius: 5px;
  height: calc(100vh - (var(--margin) * 2));
  width: calc(100vw - (var(--margin) * 2));
  position: relative;
  box-shadow: 0 10px 12px rgb(0 0 0 / 0.1);

  &.os-windows {
    border-radius: 4px;
  }

  &.os-mac {
    border-radius: 12px;
  }

  &.is-win-maximized,
  &.is-win-fullscreen {
    border-radius: 0;
    height: 100vh;
    width: 100vw;
    margin: 0;
  }
`;


export default {
  title: 'App/Window',
  component: Window,
  argTypes: {},
};

const Template = (args) => {
  const [os, setOs] = React.useState(args.os);
  const [route, setRoute] = React.useState(args.route);
  const [isElectron, setIsElectron] = React.useState(args.isElectron);
  const [isWinFullscreen, setIsWinFullscreen] = React.useState(args.isWinFullscreen);
  const [isWinFocused, setIsWinFocused] = React.useState(args.isWinFocused);
  const [isWinMaximized, setIsWinMaximized] = React.useState(args.isWinMaximized);
  const [isLeftSidebarOpen, setIsLeftOpen] = React.useState(args.isLeftSidebarOpen);
  const [isRightSidebarOpen, setIsRightSidebarOpen] = React.useState(args.isRightSidebarOpen);
  const [isCommandBarOpen, setIsCommandBarOpen] = React.useState(args.isCommandBarOpen);
  const [isMergeDialogOpen, setIsMergeDialogOpen] = React.useState(args.isMergeDialogOpen);
  const [isDatabaseDialogOpen, setIsDatabaseDialogOpen] = React.useState(args.isDatabaseDialogOpen);
  const [isThemeDark, setIsThemeDark] = React.useState(args.isThemeDark);

  return (
    <Window {...args}
      className={classnames(
        os,
        isElectron ? 'is-electron' : 'is-web',
        isWinMaximized && 'is-win-maximized',
        isWinFocused && 'is-win-focused',
        isWinFullscreen && 'is-win-fullscreen',
        isThemeDark ? 'is-theme-dark' : 'is-theme-light',
      )}
    >
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
    </Window>)
};

export const Primary = Template.bind({});
Primary.args = {
  os: () => getOs(window),
  isElectron: true
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
