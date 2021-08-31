import React, { ReactNode } from 'react';
import { Popper } from '@material-ui/core';

import { Avatar } from '../Avatar';

import { Anchor } from './components/Anchor';
import { Content } from './components/Content';
import { Toggle } from './components/Toggle';
import { Refs } from './components/Refs';
import { Container } from './components/Container';

export interface BlockProps {
  /**
   * Children
   */
  children?: any;
  /**
   * The raw content of the block
   */
  rawContent: string;
  /**
   * The rendered content of the block
   */
  renderedContent?: ReactNode;
  /**
   * Whether the block is part of a user selection
   */
  isSelected?: boolean;
  /**
   * Whether this block's children should be shown
   */
  isOpen: boolean;
  /**
   * Whether this block is locked
   */
  isLocked?: boolean;
  /**
   * Whether this block is in editing mode
   */
  isDragging?: boolean;
  /**
   * Whether this block is editable
   */
  isEditable?: boolean;
  /**
   * Whether this block is being dragged
   */
  isEditing?: boolean;
  /**
   * Number of references to this block
   */
  refsCount?: number;
  /**
   * A user attached to this block
   */
  presentUser?: { color: string, name: string };
  /**
   * The UID of this block
   */
  uid: string;
  /**
   * The UID of some other block
   */
  linkedRef?: string;
  /**
   * When toggle is pressed
   */
  handlePressToggle?: () => void;
  /**
   * When anchor is pressed
   */
  handlePressAnchor?: () => void;
  /**
   * When mouse is over block
   */
  handleMouseEnterBlock?: () => void;
  /**
   * When mouse leaves block
   */
  handleMouseLeaveBlock?: () => void;
  /**
   * When raw content of a block is modified.
   * Returns the new value of the raw content.
   */
  handleContentChange?: (e: any) => void;
  /**
   * When the content is clicked or tapped
   */
  handlePressContainer?: () => void;
  /**
   * When a dragged item is over this block
   */
  handleDragOver?: () => void;
  /**
   * When a dragged item is no longer over this block
   */
  handleDragLeave?: () => void;
  /**
   * When a dragged item dropped on this block
   */
  handleDrop?: () => void;
}

export const Block: React.FC<BlockProps> = ({
  children,
  rawContent,
  renderedContent,
  presentUser,
  isOpen,
  isSelected,
  isEditable,
  isEditing,
  isDragging,
  linkedRef,
  refsCount,
  uid,
  handleContentChange = () => null,
  handleMouseEnterBlock = () => null,
  handleMouseLeaveBlock = () => null,
  handlePressToggle = () => null,
  handlePressAnchor = () => null,
  handlePressContainer = () => null,
  handleDragOver = () => null,
  handleDragLeave = () => null,
  handleDrop = () => null,
}) => {
  const [showEditableDom, setRenderEditableDom] = React.useState(false);
  const [avatarAnchorEl, setAvatarAnchorEl] = React.useState(null);

  return (<Container
    style={presentUser ? { "--user-color": presentUser.color } : undefined}
    onClick={handlePressContainer}
    onDragOver={handleDragOver}
    onDragLeave={handleDragLeave}
    onDrop={handleDrop}
    className={[
      children ? 'show-tree-indicator' : '',
      isOpen ? 'is-open' : 'is-closed',
      linkedRef ? 'is-linked-ref' : '',
      isSelected ? 'is-selected' : '',
      isSelected && isDragging ? 'dragging' : '',
      isEditing ? 'is-editing' : ''].join(' ')}
  >
    {presentUser && (
      <Popper
        open={!!presentUser}
        anchorEl={avatarAnchorEl}
        placement="top-start"
        popperOptions={{
          modifiers: {
            preventOverflow: { enabled: false },
            offset: []
          }
        }}
      >
        <Avatar
          name={presentUser.name}
          color={presentUser.color}
          size="1.5rem"
        />
      </Popper>
    )}
    {/* Drop area indicator before */}
    <div
      className="block-body"
      ref={setAvatarAnchorEl}
      onMouseEnter={() => { handleMouseEnterBlock; setRenderEditableDom(true) }}
      onMouseLeave={() => { handleMouseLeaveBlock; setRenderEditableDom(false) }}
    >
      {children && <Toggle
        isOpen={isOpen}
        linkedRef={linkedRef}
        uid={uid}
        handlePressToggle={handlePressToggle}
      />}
      <Anchor
        anchorElement="circle"
        handlePressAnchor={handlePressAnchor}
        isClosedWithChildren={!isOpen && !!children}
      />
      {/* Tooltip el */}
      <Content
        isEditing={isEditing}
        showEditableDom={showEditableDom}
        rawContent={rawContent}
        renderedContent={renderedContent}
        handleContentChange={handleContentChange}
      />
      {/* Presence el */}
      {(refsCount >= 1) && <Refs
        refsCount={refsCount}
      />}
    </div>
    {/* inline search el */}
    {/* slash menu el */}
    {isOpen && children}
    {/* Drop area indicator child */}
    {/* Drop area indicator after */}
  </Container>)
};