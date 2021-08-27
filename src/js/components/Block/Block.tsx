import React, { ReactNode } from 'react';
import { Anchor } from './components/Anchor';
import { Content } from './components/Content';
import { Toggle } from './components/Toggle';
import { Refs } from './components/Refs';
import { Container } from './components/Container';

export interface BlockProps {
  /**
   * Whether the block has child blocks
   */
  hasChildren?: boolean;
  /**
   * The raw text content of the block
   */
  content?: string;
  /**
   * Whether the block is part of a user selection
   */
  isSelected?: boolean;
  /**
   * Children
   */
  children?: ReactNode;
  /**
   * Number of references to this block
   */
  refsCount?: number;
  /**
   * Whether this block is expanded or not
   */
  isOpen: boolean;
  /**
   * Whether this block is in editing mode
   */
  isDragging?: boolean;
  /**
   * Whether this block is being dragged
   */
  isEditing?: boolean;
  /**
   * A user attached to this block
   */
  presentUser?: { color: string };
  /**
   * The UID of this block
   */
  uid: string;
  /**
   * The UID of some other block
   */
  linkedRef?: string;
  /**
   * Switches between isOpen and !isOpen
   */
  handlePressToggle: () => void;
}

export const Block: React.FC<BlockProps> = ({
  children,
  content,
  presentUser,
  isOpen,
  isSelected,
  isEditing,
  isDragging,
  linkedRef,
  refsCount,
  handlePressToggle = () => null,
  handlePressAnchor = () => null,
  uid,
}) => (<Container
  style={presentUser ? { "--user-color": presentUser.color } : undefined}
  className={[
    children ? 'show-tree-indicator' : '',
    isOpen ? 'is-open' : 'is-closed',
    linkedRef ? 'is-linked-ref' : '',
    isSelected ? 'is-selected' : '',
    isSelected && isDragging ? 'dragging' : '',
    isEditing ? 'is-editing' : ''].join(' ')}
>
  {/* Drop area indicator before */}
  <div className="block-body">
    {children && <Toggle
      isOpen={isOpen}
      linkedRef={linkedRef}
      uid={uid}
      handlePressToggle={handlePressToggle}
    />
    }
    <Anchor
      onClick={handlePressAnchor}
      isClosedWithChildren={!isOpen && children}
      handlePressAnchor={handlePressAnchor}
    />
    {/* Tooltip el */}
    <Content
      content={content}
    />
    {/* Presence el */}
    {refsCount && <Refs
      refsCount={refsCount}
    />}
  </div>
  {/* inline search el */}
  {/* slash menu el */}
  {isOpen && children}
  {/* Drop area indicator child */}
  {/* Drop area indicator after */}
</Container>);