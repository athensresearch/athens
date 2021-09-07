import styled from 'styled-components';

import { DOMRoot } from '../../config';
import { Block } from '../Block';

import { Overlay } from '../Overlay';
import { Popper } from '@material-ui/core';

export const Preview = ({
  isPreviewOpen,
  children,
  anchorEl,
}) => <Popper
  open={isPreviewOpen}
  container={DOMRoot}
  anchorEl={anchorEl}
  popperOptions={{
    modifiers: {
      offset: {
        offset: '0, 10',
      },
    },
  }}
>
    <Preview.Container className="animate-in">
      {children}
    </Preview.Container>
  </Popper>;

Preview.Container = styled(Overlay)`
  pointer-events: none;
  padding: 0;
  font-size: var(--font-size--text-sm);
  overflow: hidden;
  width: 20em;
  line-height: 1.2;
`;

Preview.Title = styled.h1`
  font-size: var(--font-size--text-base);
  margin: 0;
  padding: 0 1rem;
  font-size: inherit;
`;

Preview.Body = styled.main`
  padding: 0 1rem 1rem;
  font-size: inherit;
`;

Preview.Media = styled.img`
  width: 100%;
  max-height: 8rem;
  object-fit: cover;
  margin-right: 1rem;
  margin-bottom: 1rem;
`;

Preview.BlockWrap = styled.div`
  overflow: hidden;
`;

Preview.BlockContent = styled.span`
  display: flex;
  line-clamp: 1;
  -webkit-line-clamp: 1;
  white-space: nowrap;
  display: -webkit-box;
  overflow: hidden;
  text-overflow: ellipsis;
`;

Preview.MiniBlock = ({ renderedContent, children }) => {
  return (
    <Preview.BlockWrap>
      <Preview.BlockContent>{renderedContent}</Preview.BlockContent>
      <div style={{ marginLeft: "1rem" }}>{children}</div>
    </Preview.BlockWrap>)
}