import React from 'react';
import { classnames } from '../../utils/classnames';

import { Shortcut } from './components/Shortcut';
import { Logo } from './components/Logo';
import { ShortcutsList } from './components/ShortcutsList';
import { Sidebar } from './components/Sidebar';
import { Version } from './components/Version';

export type Shortcut = {
  title: string;
  uid: string;
  order: number;
}

export interface LeftSidebarProps {
  isLeftSidebarOpen: boolean;
  version: string;
  shortcuts: Shortcut[];
  handlePressShortcut(): void;
}

export const LeftSidebar = ({ isLeftSidebarOpen, version, shortcuts, handlePressShortcut }: LeftSidebarProps) => {

  return (
    <Sidebar className={classnames(
      'left-sidebar',
      isLeftSidebarOpen ? 'is-left-sidebar-open' : 'is-left-sidebar-closed'
    )}>
      <div className="container">

        <ShortcutsList>
          <h2 className="heading">Shortcuts</h2>
          {shortcuts && shortcuts.map((s) => (
            <Shortcut key={s.uid} {...s} handlePressShortcut={handlePressShortcut} />
          ))}
        </ShortcutsList>

        <footer className="footer">
          <Logo rel="noreferrer" target="_blank" href="https://athensresearch.org">Athens</Logo>
          {version && <Version href="https://github.com/athensresearch/athens/blob/master/CHANGELOG.md" target="_blank" rel="noreferrer">{version}</Version>}
        </footer>
      </div>
    </Sidebar>
  )
};