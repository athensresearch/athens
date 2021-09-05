import React, { ReactNode } from 'react';
import { Popper } from '@material-ui/core';

import { DOMRoot } from '../../config';
import { classnames } from '../../utils/classnames';

import { Avatar } from '../Avatar';
import { Anchor } from './components/Anchor';
import { Content } from './components/Content';
import { Toggle } from './components/Toggle';
import { Refs } from './components/Refs';
import { Container } from './components/Container';
import { AvatarProps } from '../Avatar/Avatar';

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
  isOpen?: boolean;
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
   * Whether to display the avatar of a present user
   */
  showPresentUserAvatar?: boolean;
  /**
   * A user attached to this block
   */
  presentUser?: AvatarProps;
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

export const Block = ({
  children,
  rawContent,
  renderedContent,
  presentUser,
  showPresentUserAvatar = true,
  isOpen = true,
  isSelected,
  isEditable,
  isEditing,
  isLocked,
  isDragging,
  linkedRef,
  refsCount,
  uid,
  handleContentChange,
  handleMouseEnterBlock,
  handleMouseLeaveBlock,
  handlePressToggle,
  handlePressAnchor,
  handlePressContainer,
  handleDragOver,
  handleDragLeave,
  handleDrop,
}: BlockProps) => {
  const [showEditableDom, setRenderEditableDom] = React.useState(false);
  const [avatarAnchorEl, setAvatarAnchorEl] = React.useState(null);

  return (<>
    <Container
      style={(showPresentUserAvatar && presentUser) ? { "--user-color": presentUser.color } : undefined}
      onClick={handlePressContainer}
      onDragOver={handleDragOver}
      onDragLeave={handleDragLeave}
      onDrop={handleDrop}
      className={classnames(
        children && 'show-tree-indicator',
        isOpen ? 'is-open' : 'is-closed',
        linkedRef && 'is-linked-ref',
        isLocked && 'is-locked',
        isSelected && 'is-selected',
        presentUser && showPresentUserAvatar && 'is-presence',
        isSelected && isDragging && 'is-dragging',
        isEditing && 'is-editing')}
  >
    {/* Drop area indicator before */}
    <div
      className="block-body"
        ref={showPresentUserAvatar && setAvatarAnchorEl}
        onMouseEnter={() => { handleMouseEnterBlock; isEditable && setRenderEditableDom(true) }}
        onMouseLeave={() => { handleMouseLeaveBlock; isEditable && setRenderEditableDom(false) }}
    >
        {children && !isLocked && <Toggle
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
      {(refsCount >= 1) && <Refs
        refsCount={refsCount}
      />}
    </div>
    {/* inline search el */}
    {/* slash menu el */}
    {isOpen && children}
    {/* Drop area indicator child */}
    {/* Drop area indicator after */}

    </Container>

    {(showPresentUserAvatar && presentUser) && (
      <>
      <Popper
          open={true}
        anchorEl={avatarAnchorEl}
        placement="top-start"
          container={DOMRoot}
          modifiers={{
            offset: { offset: "2, -32" },
            flip: { enabled: false },
            preventOverflow: {
              enabled: false,
            }
        }}
      >
        <Avatar
          {...presentUser}
          size="1.5rem"
            style={{ filter: "drop-shadow(0 2px 4px rgb(0 0 0 / 0.1))", zIndex: 99999 }}
        />
        </Popper>
      </>
    )}
  </>)
};