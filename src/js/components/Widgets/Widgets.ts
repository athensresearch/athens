import styled from 'styled-components';

export const Widgets = () => null``;

Widgets.BidirectionalLink = styled.a`
  display: inline-flex;
  color: var(--link-color);
  margin-inline: calc(-0.25em + 0.1ch);
  padding-inline: calc(0.25em);
  border-radius: 0.25em;
  text-decoration: none;
  cursor: pointer;
  transition: background 0.1s ease-in-out;

  &:hover {
    opacity: var(--opacity-higher);
  }

  &:active {
    opacity: var(--opacity-high);
    user-select: none;
  }

  &:before,
  &:after {
    color: var(--link-color---opacity-low);
    letter-spacing: -0.2ch;
  }

  &:before {
    content: '[[';
    margin-inline-end: 0.1ch;
  }

  &:after {
    content: ']]';
    margin-inline-start: 0.1ch;
  }
`;