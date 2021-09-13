import React from 'react';
import styled from 'styled-components';
import { Popper } from '@material-ui/core';

import { DOMRoot } from '../../../config';
import { classnames } from '../../../utils/classnames';

import { Avatar } from '../../Avatar';
import { Anchor } from './components/Anchor';
import { Body } from './components/Body';
import { Content, ContentProps } from './components/Content';
import { Toggle } from './components/Toggle';
import { Refs } from './components/Refs';
import { Container } from './components/Container';

export interface BlockProps extends Block, ContentProps {
    /**
     * Whether this block is in editing mode
     */
    isDragging?: boolean;
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
    showPresentUser?: boolean;
    /**
     * 
     */
    linkedRef?: string;
    /**
     * When toggle is pressed
     */
    handlePressToggle?: (uid) => void;
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
    showPresentUser = true,
    isOpen = true,
    isSelected,
    isEditable,
    isEditing,
    isLocked,
    isDragging,
    linkedRef,
    refsCount,
    uid,
    contentProps,
    textareaProps,
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
    const [showEditableDom, setRenderEditableDom] = React.useState<boolean>(false);
    const [avatarAnchorEl, setAvatarAnchorEl] = React.useState<HTMLDivElement | null>(null);

    return (<>
        <Container
            style={(showPresentUser && presentUser) ? { "--user-color": presentUser.color } : undefined}
            onClick={(e) => { e.stopPropagation(); handlePressContainer && handlePressContainer(e); }}
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
            onDrop={handleDrop}
            className={classnames(
                children && 'show-tree-indicator',
                isOpen ? 'is-open' : 'is-closed',
                linkedRef && 'is-linked-ref',
                isLocked && 'is-locked',
                isSelected && 'is-selected',
                presentUser && showPresentUser && 'is-presence',
                isSelected && isDragging && 'is-dragging',
                isEditing && 'is-editing')}
        >
            {/* Drop area indicator before */}
            <Body
                ref={showPresentUser && setAvatarAnchorEl}
                onMouseEnter={() => { handleMouseEnterBlock; isEditable && setRenderEditableDom(true) }}
                onMouseLeave={() => { handleMouseLeaveBlock; isEditable && setRenderEditableDom(false) }}
            >
                {children && !isLocked && (
                    <Toggle
                        isOpen={isOpen}
                        uid={uid}
                        handlePressToggle={handlePressToggle}
                    />)}
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
                    contentProps={contentProps}
                    textareaProps={textareaProps}
                />
                {(refsCount >= 1) && <Refs
                    refsCount={refsCount}
                />}
            </Body>
            {/* inline search el */}
            {/* slash menu el */}
            {isOpen && children}
            {/* Drop area indicator child */}
            {/* Drop area indicator after */}

        </Container>

        {(showPresentUser && presentUser) && (
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
                        style={{ filter: "drop-shadow(0 2px 2px var(--shadow-color---opacity-lower))" }}
                    />
                </Popper>
            </>
        )}
    </>)
};

Block.Anchor = Anchor;
Block.Container = Container;
Block.Toggle = Toggle;
Block.Body = Body;
Block.Content = Content;
Block.ListContainer = styled.div`
    display: flex;
    flex-direction: column;
`;
