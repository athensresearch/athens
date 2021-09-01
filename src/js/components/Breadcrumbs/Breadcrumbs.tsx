import React from 'react';
import styled from 'styled-components';

const BreadcrumbsWrap = styled.ol`
  display: flex;
  align-items: center;
  gap: 0.1rem;
  list-style: none;
  padding: 0;
  margin: 0;

  &:hover {
    a {
      opacity: 0.7;

      &:hover {
        opacity: 1;
      }
    }
  }
`;

const ItemWrap = styled.a`
  text-decoration: none;
  color: inherit;
  transition: opacity 0.2s ease-in-out;

  * {
    pointer-events: none;
  }
`;

const SeparatorSVG = styled.svg`
  width: 1em;
  height: 1em;
  color: var(--body-text-color---opacity-med);
  flex: 0 0 auto;
`;

const Separator = () => <SeparatorSVG viewBox="0 0 24 24">
  <path d="M9 6L15 12L9 18" stroke="currentColor" fill="none" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
</SeparatorSVG>

interface BreadcrumbsProps extends React.OlHTMLAttributes<HTMLOListElement> {

}
/** 
 * Show a list of breadcrumbs indicating an item's larger context.
 */
export const Breadcrumbs = ({ children, ...props }: BreadcrumbsProps) => {

  return (
    <BreadcrumbsWrap {...props}>
      {typeof children === 'string'
        ? <ItemWrap>{children}</ItemWrap>
        : React.Children.map(children, (item, index) => (<>
          {index !== 0 && (<Separator />)}
          {React.cloneElement(item, {
            key: index.toString() + item,
          })}
        </>))}
    </BreadcrumbsWrap>
  );
};
Breadcrumbs.Item = ItemWrap;