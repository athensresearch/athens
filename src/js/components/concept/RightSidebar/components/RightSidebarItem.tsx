import React from "react";
import styled from 'styled-components';
import { Button } from '../../../Button'
import { Close, ChevronRight, BubbleChart, Description, FiberManualRecord } from "@material-ui/icons";

const ItemWrap = styled.article`
  display: flex;
  flex: 0 0 auto;
  flex-direction: column;
`;

const Toggle = styled(Button)`
  margin: auto 0.5rem auto 0;
  flex: 0 0 auto;
  width: 1.75rem;
  height: 1.75rem;
  padding: 0;
  cursor: pointer;
  border-radius: 1000px;
  place-content: center;

  svg {
    transition: transform 0.1s ease-out;
    margin: 0;
  }

  .is-item-open & {
    svg {
      transform: rotate(90deg);
    }
  }
`;

const Container = styled.div`
  line-height: 1.5rem;
  z-index: 1;
  padding: 0 0 1.25rem;
  font-size: 95%;
  position: relative;
  background: inherit;

  h1 {
    font-size: 1.5em;
    display: --webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 1;
    line-clamp: 1;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .node-page,
  .block-page {
    margin-top: 0;
  }
`;

const Header = styled.header`
  font-size: 100%;
  align-items: center;
  z-index: 2;
  box-shadow: 0 -1px 0 0 var(--border-color);
  display: flex;
  flex: 0 0 auto;
  padding: 0.25rem 1rem;
  position: sticky;
  background: var(--background-color);
  top: 0;
  bottom: 0;

  h2 {
    font-size: inherit;
    flex: 1 1 100%;
    line-height: 1;
    margin: 0;
    white-space: nowrap;
    text-overflow: ellipsis;
    font-weight: normal;
    max-width: 100%;
    overflow: hidden;
    align-items: center;
    color: var(--body-text-color);

    svg {
      opacity: var(--opacity-med);
      display: inline;
      vertical-align: -4px;
      margin-right: 0.2em;
    }
  }

  .controls {
    display: flex;
    flex: 0 0 auto;
    align-items: stretch;
    transition: opacity 0.3s ease-out;
    opacity: 0.5;

    :hover & {
      opacity: 1;
    }
  }

  svg {
    font-size: 18px;
  }

  hr {
    width: 1px;
    background: var(--background-minus-1);
    border: 0;
    margin: 0.25rem;
    flex: 0 0 1px;
    height: 1em;
    justify-self: stretch;
  }

  .is-item-open & {
    h2 {
      font-weight: 500;
    }
  }

`;

export type RightSidebarItem = {
  isOpen: boolean;
  body: React.ReactNode;
  title: React.ReactNode;
  type: 'block' | 'page' | 'graph';
  handlePressItemToggle: () => void;
  handlePressItemClose: () => void;
};

export const Item = ({
  isOpen,
  title,
  body,
  type,
  handlePressItemToggle,
  handlePressItemClose,
}) => {

  let icon;
  switch (type) {
    case 'block':
      icon = <FiberManualRecord />;
    case 'page':
      icon = <Description />;
    case 'graph':
      icon = <BubbleChart />;
  }

  return (<ItemWrap className={isOpen ? 'is-item-open' : 'is-item-closed'}>
    <Header>
      <Toggle onClick={handlePressItemToggle}><ChevronRight /></Toggle>
      <h2>{icon} {title}</h2>
      <Button onClick={handlePressItemClose}><Close /></Button>
    </Header>
    {isOpen && <Container>{body}</Container>}
  </ItemWrap>)
};
