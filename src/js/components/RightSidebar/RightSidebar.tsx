import React from "react";
import styled from 'styled-components';
import { classnames } from '../../util/classnames';
import { EmptyMessage } from "./components/EmptyMessage";
import { RightSidebarItem, Item } from "./components/RightSidebarItem";

const Sidebar = styled.aside`
  justify-self: stretch;
  overflow: hidden;
  width: 0;
  grid-area: secondary-content;
  display: flex;
  justify-content: space-between;
  padding-top: 2.75rem;
  transition-property: width, border, background;
  transition-duration: 0.35s;
  transition-timing-function: ease-out;
  box-shadow: 0 -100px 0 var(--background-minus-1), inset 1px 0 var(--background-minus-1);

  svg {
    color: var(--body-text-color---opacity-high);
  }

  &.is-right-sidebar-closed {
    width: 0;
  }

  &.is-right-sidebar-open {
    width: var(--width);
  }

  &::-webkit-scrollbar {
    background: var(--background-minus-1);
    width: 0.5rem;
    height: 0.5rem;
  }
  &::-webkit-scrollbar-corner {
    background: var(--background-minus-1);
  }
  &::-webkit-scrollbar-thumb {
    background: var(--background-plus-1);
    border-radius: 0.5rem;
  }

  > .content {
    display: flex;
    flex: 1 1 var(--width);
    flex-direction: column;
    margin-left: 0;
    overflow-y: auto;

    @supports (overflow-y: overlay) {
      overflow-y: overlay;
    }
    
    .is-right-sidebar-closed & {
      margin-left: calc(var(--width) * -1);
      opacity: 0;
    }

    .is-right-sidebar-open & {
      opacity: 1;
    }
  }
`;

const DragHandle = styled.div`
  cursor: col-resize;
  height: 100%;
  position: absolute;
  top: 0;
  width: 1px;
  z-index: var(--z-index-fixed);
  background: var(--border-color);

  &:after {
    content: '';
    position: absolute;
    background: var(--link-color);
    top: 0;
    bottom: 0;
    left: 0;
    right: -4px;
    opacity: 0;
  }

  &:hover:after {
    opacity: 0.5;
  }

  .is-resizing &:after {
    opacity: 1;
  }
`;


export interface RightSidebarProps extends React.HTMLAttributes<HTMLDivElement> {
  /**
   * Whether Right Sidebar is open
   */
  isRightSidebarOpen: boolean;
  /**
   * Whether Right Sidebar is being resized
   */
  isResizing?: boolean;
  /**
   * Width of Right Sidebar in viewport width units
   */
  width?: number;
  /**
   * Width of Right Sidebar in viewport width units
   */
  items?: RightSidebarItem[];
  handleGrabDragHandle?(): void;
  handlePressItemClose?(): void;
  handlePressItemToggle?(): void;
}

export const RightSidebar = ({
  isRightSidebarOpen,
  isResizing = false,
  items,
  width = 300,
  handleGrabDragHandle,
  handlePressItemClose,
  handlePressItemToggle
}: RightSidebarProps) => {
  return (<Sidebar
    className={classnames(
      isRightSidebarOpen ? 'is-right-sidebar-open' : 'is-right-sidebar-closed',
      isResizing && 'is-resizing'
    )}
    style={{ "--width": width + 'px' }}
  >
    <DragHandle
      onMouseDown={handleGrabDragHandle}
    />
    <div className="content">
      {items
        ? items.map((item) => <Item
          isOpen={item.isOpen}
          handlePressItemClose={handlePressItemClose}
          handlePressItemToggle={handlePressItemToggle}
          type={item.type}
          title={item.title}
          body={item.body}
        />)
        : <EmptyMessage />}
    </div>
  </Sidebar>)
}