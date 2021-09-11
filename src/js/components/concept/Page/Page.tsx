import React from 'react';
import styled from 'styled-components';

import { Today, MoreHoriz, Delete, Bookmark, BubbleChart } from '@material-ui/icons';
import { Popper, Modal } from "@material-ui/core";

import { DOMRoot } from '../../../config';

import { Button } from '../../Button';
import { Overlay } from '../../Overlay';
import { Menu } from '../../Menu';
import { Block } from '../Block';

import { EmptyMessage } from './components/EmptyMessage';
import { References, ReferencesProps } from './components/References';

const PageWrap = styled.article`
  padding: 1rem 0;
  flex-basis: 100%;
  align-self: stretch;
  width: 100%;
  max-width: 60em;
  margin-left: auto;
  margin-right: auto;
`;

const PageHeader = styled.header`
  position: relative;
  padding: 0 3rem;
`;

const Title = styled.h1`
  font-size: 2.5rem;
  position: relative;
  overflow: visible;
  flex-grow: 1;
  margin: 0.1em 0;
  white-space: pre-line;
  word-break: break-word;
  line-height: 1.1em;

  textarea {
    padding: 0;
    margin: 0;
    width: 100%;
    min-height: 100%;
    font-weight: inherit;
    letter-spacing: inherit;
    font-size: inherit;
    appearance: none;
    cursor: text;
    resize: none;
    transform: translate3d(0,0,0);
    color: inherit;
    caret-color: var(--link-color);
    background: transparent;
    line-height: inherit;
    border: 0;
    font-family: inherit;
    visibility: hidden;
    position: absolute;

    &::webkit-scrollbar {
      display: none;
    }

    &:focus,
    &.is-editing {
      outline: none;
      visibility: visible;
      position: relative;
    }

    abbr {
      z-index: 4;
    }

    &.is-editing + span {
      visibility: hidden;
      position: absolute;
    }
  }
`;

const PageMenuToggle = styled(Button)`
  float: left;
  border-radius: 1000px;
  margin-left: -2.5rem;
  margin-top: 0.5rem;
  width: 2rem;
  height: 2rem;
  color: var(--body-text-color---opacity-high);
  vertical-align: bottom;
`;


const BlocksContainer = styled(Block.ListContainer)`
  padding-left: 1rem;
  padding-right: 1rem;
  display: flex;
  flex-direction: column;
`;

interface PageProps extends ReferencesProps {
  children?: React.ReactNode,
  /**
   * Whether the page is a Daily Note
   */
  isDailyNote: boolean,
  /**
   * Whether the page has a corresponding shortcut
   */
  hasShortcut?: boolean,
  /**
   * The title of the page
   */
  title: React.ReactNode,
  /**
   * Whether the page can be edited
   */
  isEditable?: boolean,
  /**
   * The unique identifier of the page
   */
  uid: string,
  handlePressRemoveShortcut(): void,
  handlePressAddShortcut(): void,
  handlePressShowLocalGraph(): void,
  handlePressDelete(): void,
}


/**
 * Display whole page content
 */
export const Page = ({
  isDailyNote,
  hasShortcut,
  isEditable = true,
  children,
  title,
  uid,
  isLinkedReferencesOpen,
  isUnlinkedReferencesOpen,
  handlePressLinkedReferencesToggle,
  handlePressUnlinkedReferencesToggle,
  handlePressRemoveShortcut,
  handlePressAddShortcut,
  handlePressShowLocalGraph,
  handlePressDelete,
}: PageProps) => {
  const [isPageMenuOpen, setIsPageMenuOpen] = React.useState(false);
  const [pageMenuAnchor, setPageMenuAnchor] = React.useState(null);

  const handlePressMenuToggle = (e) => {
    setPageMenuAnchor(e.currentTarget);
    setIsPageMenuOpen(true);
  };

  const handleClosePageMenu = () => {
    setPageMenuAnchor(null);
    setIsPageMenuOpen(false);
  };

  return (
    <PageWrap className={isDailyNote ? 'is-daily-note' : ''}>
      <PageHeader>

        <Title>
          <PageMenuToggle
            shape="round"
            isPressed={isPageMenuOpen}
            onClick={handlePressMenuToggle}
          >
            <MoreHoriz />
          </PageMenuToggle>
          <Modal
            onClose={handleClosePageMenu}
            BackdropProps={{ invisible: true }}
            container={DOMRoot}
            open={isPageMenuOpen}
          >
            <Popper
              open={isPageMenuOpen}
              anchorEl={pageMenuAnchor}
              disablePortal={true}
              placement="bottom-start"
            >
              <Overlay className="animate-in">
                <Menu>
                  {hasShortcut
                    ? <Button onClick={handlePressRemoveShortcut}><Bookmark /> <span>Remove Shortcut</span></Button>
                    : <Button onClick={handlePressAddShortcut}><Bookmark /> <span>Add Shortcut</span></Button>}
                  <Button onClick={handlePressShowLocalGraph}><BubbleChart /> <span>Show Local Graph</span></Button>
                  <Menu.Separator />
                  <Button onClick={handlePressDelete}><Delete /> <span>Delete Page</span></Button>
                </Menu>
              </Overlay>
            </Popper>
          </Modal>
          {title} {isDailyNote && <Today />}</Title>
      </PageHeader>
      {children ? children : isEditable ? <BlocksContainer><Block /></BlocksContainer> : <EmptyMessage />}
      <References
        isLinkedReferencesOpen={isLinkedReferencesOpen}
        isUnlinkedReferencesOpen={isUnlinkedReferencesOpen}
        handlePressLinkedReferencesToggle={handlePressLinkedReferencesToggle}
        handlePressUnlinkedReferencesToggle={handlePressUnlinkedReferencesToggle}
      />
    </PageWrap>);
};

Page.BlocksContainer = BlocksContainer;