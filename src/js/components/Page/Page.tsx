import React from 'react';
import styled from 'styled-components';
import { MoreHoriz, Delete, Bookmark, BubbleChart } from '@material-ui/icons';
import { Popper, Modal } from "@material-ui/core";
import { Button } from '../Button';
import { Overlay } from '../Overlay';
import { Menu } from '../Menu';

const MainContent = styled.div`
  flex: 1 1 100%;
  grid-area: main-content;
  align-items: flex-start;
  justify-content: stretch;
  padding-top: 2.5rem;
  display: flex;
  overflow-y: auto;

  @supports (overflow-y: overlay) {
    overflow-y: overlay;
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
    background: var(--background-minus-2);
  }
`;

const PageWrap = styled.article`
  margin: 2rem auto;
  padding: 1rem 2rem 10rem 2rem;
  flex-basis: 100%;
  align-self: stretch;
  max-width: 55rem;
`;

const PageHeader = styled.header`
  position: relative;
`;

const Title = styled.h1`
  position: relative;
  overflow: visible;
  flex-grow: 1;
  margin: 0.1em 0 0.1em 1rem;
  letter-spacing: --0.03em;
  white-space: pre-line;
  word-break: break-word;
  line-height: 1.4em;

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
  position: absolute;
  left: -1.5rem;
  border-radius: 1000px;
  padding: 0.375rem 0.5rem;
  color: var(--body-text-color---opacity-high);
  transform: translateY(-50%);
  top: 50%;
`;

interface PageProps {
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
   * The unique identifier of the page
   */
  uid: string,
}


/**
 * Display whole page content
 */
export const Page = ({
  isDailyNote,
  hasShortcut,
  children,
  title,
  uid,
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
    <MainContent>
      <PageWrap>
        <PageHeader>
          <PageMenuToggle
            isPressed={isPageMenuOpen}
            onClick={handlePressMenuToggle}
          >
            <MoreHoriz />
          </PageMenuToggle>
          <Modal
            onClose={handleClosePageMenu}
            BackdropProps={{ invisible: true }}
            container={() => document.querySelector('#app')}
            open={isPageMenuOpen}
          >
            <Popper
              open={isPageMenuOpen}
              anchorEl={pageMenuAnchor}
              disablePortal={true}
              placement="bottom-start"
            >
              <Overlay>
                <Menu>
                  {hasShortcut
                    ? <Button><Bookmark /> Remove Shortcut</Button>
                    : <Button><Bookmark /> Add Shortcut</Button>}
                  <Button><BubbleChart /> Show Local Graph</Button>
                  <Menu.Separator />
                  <Button><Delete /> Delete Page</Button>
                </Menu>
              </Overlay>
            </Popper>
          </Modal>

          <Title>{title}</Title>
        </PageHeader>
        {children ? children : <div>placeholder</div>}
      </PageWrap>
    </MainContent>);
};