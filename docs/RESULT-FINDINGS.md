A aplicação foi testada com diferentes ai providers: openai, claude e gemini.
Durante os testes foram validadas as funcionalidades da UI do catálogo e do Carrinho. 
Funcionalidades de interação com a UI foram validadas com sucesso.
Testes de funcionalidades de interação com o chat de provedores de AI estão descritos nas seções abaixo.


## Testes com gemini
Em interações com o chat do gemini foi possível validar que AI agiu conforme o objetivo atribuído à mesma.
Porém produzia resultados alucionatórios quando solicitada que procurasse itens no catálogo, adicionesse itens no carrinho ou finalizasse o chechout 

O esperado seria que a LLM interagisse com as funções/[tools](https://docs.spring.io/spring-ai/reference/api/tools.html) disponibilizadas pelo serviço backend.

Mesmo atribuindo `temperature` 0 o gemini continuou dando respostas alucinatórias.
`Temperature` é um parâmetro utilizado para controlar a randomicidade e criatividade das respostas geradas com LLM.
Valores mais baixos levam à resultados mais determinísticos. Enquanto que valores mais altos resultam em respostas mais criativas e inesperadas.


## Testes com openai
Durante a execução dos testes com a openai, na interação com o chat foram obtidos erros 429, quota excedida.
Portanto os resultados com a openai foram inconclusivos.
```
429 - {
    "error": {
        "message": "You exceeded your current quota, please check your plan and billing details. For more information on this error, read the docs: https://platform.openai.com/docs/guides/error-codes/api-errors.",
        "type": "insufficient_quota",
        "param": null,
        "code": "insufficient_quota"
    }
}
```

## Testes com claude
Similarmente aos testes executados com openai, na interação com o chat do claude foram obtidos erros de falta de saldo.
```
{
    "type":"error",
    "error": {
        "type":"invalid_request_error",
        "message":"Your credit balance is too low to access the Anthropic API. Please go to Plans & Billing to upgrade or purchase credits."
    },
    "request_id":"req_011CUjiiMgynRWUQydEM2qQV"
}
```

# Próximos passos

1. Obter API keys válidas para testar o projeto com outros provedores de AI.
2. Implementar o projeto usando AI frameworks alternativos, ex: [Langchain](https://reference.langchain.com/python/)