import styled from 'styled-components';

export const Wrap = styled.div`
  border-top: 1px solid var(--border-color);
  padding: 2rem 0;
  line-height: 1.25;
  display: grid;
  grid-template-columns: 10rem 1fr;
  grid-template-areas: 'header body';

  &.disabled {
    opacity: 0.5;
  }
`;

export const Label = styled.label`
  display: flex;
  align-items: center;
  font-weight: bold;
  gap: 0.5rem;
`

export const Header = styled.header`
  grid-area: header;
  padding-bottom: 1rem;
`;

export const Title = styled.h3`
  margin: 0;
`;

export const Body = styled.main`
  grid-area: body;
`;

export const Glance = styled.span`
  font-weight: normal;
  opacity: var(--opacity-high);
  font-size: 0.8em;
  gap: 0.25em;

  svg {
    vertical-align: -0.25rem;
    font-size: 1.5em;
  }
`;

export const Details = styled.aside`
  grid-area: details;
  font-size: 0.8em;
  padding-top: 0.5rem;

  p {
    margin: 0.25rem 0;

    &:first-child {
      margin-top: 0;
    }
    &:last-child {
      margin-bottom: 0;
    }
  }
`;