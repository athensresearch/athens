import styled from 'styled-components';

import { Breadcrumbs } from '../../../../Breadcrumbs';

import { Block } from '../../../../Block';

const ReferenceWrap = styled.li`
  list-style: none;
  padding-top: 0.25rem;
  padding-bottom: 0.25rem;
`;

interface ReferenceBlockProps extends Block {
  isOpen,
  uid,
  renderedContent,
  handlePressToggle()
  rawContent,
}

export const Reference = ({
  uid,
  isOpen,
  renderedContent,
  handlePressToggle,
  rawContent
}: ReferenceBlockProps) => {
  return (<ReferenceWrap>
    <Breadcrumbs>
      <Breadcrumbs.Item>page name</Breadcrumbs.Item>
      <Breadcrumbs.Item>page name</Breadcrumbs.Item>
      <Breadcrumbs.Item>page name</Breadcrumbs.Item>
    </Breadcrumbs>
    <Block.Container>
      <Block.Body>
        <Block.Toggle
          uid={uid}
          isOpen={isOpen}
          handlePressToggle={() => null}
          isClosedWithChildren={false}
        />
        <Block.Anchor
          handlePressAnchor={() => null}
          isClosedWithChildren={false}
        />
        <Block.Content
          showEditableDom={false}
          renderedContent={renderedContent}
          rawContent={rawContent} />
      </Block.Body>
    </Block.Container>
  </ReferenceWrap>);
};
