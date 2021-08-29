import React from 'react';
import styled from 'styled-components';
import { MoreHoriz } from '@material-ui/icons';
import { Popper } from "@material-ui/core";
import ClickAwayListener from '@material-ui/core/ClickAwayListener';
import { Button } from '../Button';
import { Overlay } from '../Overlay';
import { Menu } from '../Menu';

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
  isDailyNote: boolean,
  hasShortcut: boolean,
  title: React.ReactNode,
  uid: string,
}

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

  return (<PageWrap>
    <PageHeader>
      <PageMenuToggle
        isPressed={isPageMenuOpen}
        onClick={handlePressMenuToggle}
      >
        <MoreHoriz />
      </PageMenuToggle>
      <Popper
        open={isPageMenuOpen}
        anchorEl={pageMenuAnchor}
        container={() => document.querySelector('#app')}
        placement="bottom-start"
      >
        <ClickAwayListener onClickAway={handleClosePageMenu}>
          <Overlay>
            <Menu>
              <Button>test button</Button>
              <Button>test button</Button>
              <Menu.Separator />
              <Button>test button</Button>
            </Menu>
          </Overlay>
        </ClickAwayListener>
      </Popper>

      <Title>{title}</Title>
    </PageHeader>
    {children ? children : <div>placeholder</div>}
  </PageWrap>);
};