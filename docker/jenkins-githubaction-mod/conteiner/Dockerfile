FROM python:3.7.13-slim AS builder
ADD . /app
WORKDIR /app

# We are installing a dependency here directly into our app source dir
RUN pip install --target=/app api4jenkins==1.11 requests==2.28.1

# A distroless container image with Python and some basics like SSL certificates
# https://github.com/GoogleContainerTools/distroless
FROM gcr.io/distroless/python3-debian10
COPY --from=builder /app /app
WORKDIR /app
ENV PYTHONPATH /app


ARG INPUT_URL INPUT_USERNAME INPUT_API_TOKEN

RUN if [ -z "$INPUT_URL" ] || [ -z "$INPUT_USERNAME" ] || [ -z "$INPUT_API_TOKEN" ]; then echo Segredos INPUT_URL INPUT_USERNAME e INPUT_API_TOKEN nao informados; exit 1; fi

ENV INPUT_URL=$INPUT_URL \
    INPUT_USERNAME=$INPUT_USERNAME \
    INPUT_API_TOKEN=$INPUT_API_TOKEN

CMD ["/app/main.py"]